package com.tvkdevelopment.titanirc.irc

import com.tvkdevelopment.titanirc.TitanircConfiguration
import com.tvkdevelopment.titanirc.bridge.BridgeClient
import com.tvkdevelopment.titanirc.irc.listeners.*
import com.tvkdevelopment.titanirc.util.Log
import kotlinx.coroutines.*
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.delay.AdaptingDelay
import org.pircbotx.hooks.managers.SequentialListenerManager
import kotlin.time.Duration.Companion.seconds

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
class Irc(private val configuration: TitanircConfiguration) : BridgeClient {

    override val name = "IRC"

    private val connectionScope = CoroutineScope(SupervisorJob() + newSingleThreadContext("Irc"))
    private val taskScope = CoroutineScope(newSingleThreadContext("IrcTasks"))
    private val healthScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var connectJob: Job? = null
    private val messageSender = IrcMessageSender(taskScope, configuration, ::bot)

    @Volatile private var bot: PircBotX? = null
    private var maxLineLength: Int = QUAKENET_MAX_LINE_LENGTH

    private val bridgeListeners = mutableListOf<BridgeClient.Listener>()

    override fun connect() {
        // startBot() blocks the connection thread while PircBotX reconnects. Stop it from
        // the caller's thread before queuing a replacement on that same connection thread.
        val previousBot = bot
        previousBot?.stopBotReconnect()
        previousBot?.close()
        connectJob?.cancel()
        connectJob = connectionScope.launch {
            while (isActive) {
                var currentBot: PircBotX? = null
                try {
                    val newBot = PircBotX(Configuration.Builder().apply {
                        name = configuration.ircNick
                        login = configuration.ircUsername
                        realName = "Dopelives bridge"
                        configuration.ircPassword?.let {
                            nickservCustomMessage =
                                "PRIVMSG Q@CServe.quakenet.org :AUTH ${configuration.ircUsername} $it"
                        }

                        addServer("irc.quakenet.org")
                        configuration.ircChannels.forEach {
                            addAutoJoinChannel(it)
                        }
                        isAutoReconnect = true
                        autoReconnectAttempts = Int.MAX_VALUE
                        autoReconnectDelay = AdaptingDelay(2_000L, 30_000L).also { addListener(it) }
                        socketConnectTimeout = 15_000
                        isUserModeHideRealHost = true
                        isAutoNickChange = true

                        setListenerManager(SequentialListenerManager.newDefault())

                        addListener(LogListener())
                        addListener(IrcHealthListener(healthScope, configuration.ircChannels) { failedBot ->
                            // Closing the socket makes PircBotX run its own reconnect loop.
                            if (bot === failedBot) failedBot.close()
                        })
                        addListener(NickFixListener(configuration))
                        addListener(IrcBridgeListener(bridgeListeners))
                        addListener(messageSender)
                        addListener(AdminListener(configuration, messageSender))
                    }.buildConfiguration())
                    currentBot = newBot
                    bot = newBot
                    newBot.startBot()
                    Log.w("IRC connection loop ended; restarting")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("IRC connection loop failed; restarting", e)
                } finally {
                    if (bot === currentBot) bot = null
                    currentBot?.stopBotReconnect()
                    currentBot?.close()
                }
                delay(CRASH_RESTART_DELAY)
            }
        }
    }

    override fun addBridgeListener(listener: BridgeClient.Listener) {
        bridgeListeners += listener
    }

    override fun relayMessage(channel: String, nick: String, message: String) {
        sendMessage(
            channel,
            "<$nick> ".takeUnless { message.startsWith('!') },
            message,
        )
    }

    override fun relaySlashMe(channel: String, nick: String, message: String) {
        sendMessage(channel, "* $nick ", message)
    }

    private fun sendMessage(channel: String, prefix: String?, message: String) {
        message
            .replace(REGEX_BLANK_LINE_REPLACE, "\n")
            .conditionalTransform(message.count { it == '\n' } >= MAX_LINES_BEFORE_JOINING) {
                it.replace(REGEX_NEW_LINE_REPLACE, " ")
            }
            .splitMessageForIrc(maxLineLength, prefix = prefix)
            .forEach { messageSender.sendMessage(channel, it) }
    }

    override fun setTopic(channel: String, topic: String) {
        messageSender.setTopic(channel, topic)
    }

    companion object {
        private const val QUAKENET_MAX_LINE_LENGTH = 440
        private val CRASH_RESTART_DELAY = 5.seconds

        private const val MAX_LINES_BEFORE_JOINING = 5

        private val REGEX_BLANK_LINE_REPLACE = Regex("\n{2,}")
        private val REGEX_NEW_LINE_REPLACE = Regex("\n+")

        private fun <T> T.conditionalTransform(condition: Boolean, transform: (T) -> T): T =
            if (condition) transform(this) else this
    }
}
