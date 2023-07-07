package com.tvkdevelopment.titanirc.irc

import com.tvkdevelopment.titanirc.TitanircConfiguration
import com.tvkdevelopment.titanirc.bridge.BridgeClient
import com.tvkdevelopment.titanirc.irc.listeners.IrcBridgeListener
import com.tvkdevelopment.titanirc.irc.listeners.LogListener
import com.tvkdevelopment.titanirc.irc.listeners.NickFixListener
import com.tvkdevelopment.titanirc.irc.listeners.RestartListener
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

    private val scope = CoroutineScope(newSingleThreadContext("Irc"))
    private var connectJob: Job? = null
    private val messageSender = IrcMessageSender(configuration, ::bot)

    private var bot: PircBotX? = null
    private var maxLineLength: Int = QUAKENET_MAX_LINE_LENGTH

    private val bridgeListeners = mutableListOf<BridgeClient.Listener>()

    override fun connect() {
        connectJob?.cancel()
        connectJob = scope.launch {
            bot?.close()
            try {
                PircBotX(Configuration.Builder().apply {
                    name = configuration.ircNick
                    login = configuration.ircUsername
                    realName = "Dopelives bridge"
                    configuration.ircPassword?.let {
                        nickservCustomMessage =
                            "PRIVMSG Q@CServe.quakenet.org :AUTH ${configuration.ircUsername} $it"
                    }

                    addServer("port80c.se.quakenet.org")
                    configuration.ircChannels.forEach {
                        addAutoJoinChannel(it)
                    }
                    isAutoReconnect = true
                    autoReconnectAttempts = 10000
                    autoReconnectDelay = AdaptingDelay(2_000L, 30_000L).also { addListener(it) }
                    isUserModeHideRealHost = true
                    isAutoNickChange = true

                    setListenerManager(SequentialListenerManager.newDefault())

                    addListener(LogListener())
                    addListener(RestartListener(DISCONNECT_RESTART_DELAY, ::connect))
                    addListener(NickFixListener(name))
                    addListener(IrcBridgeListener(bridgeListeners))
                    addListener(messageSender)
                }.buildConfiguration())
                    .also { bot = it }
                    .startBot()
            } catch (e: Exception) {
                Log.e(e)
                delay(CRASH_RESTART_DELAY)
                connect()
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
        private const val QUAKENET_MAX_LINE_LENGTH = 443
        private val DISCONNECT_RESTART_DELAY = 30.seconds
        private val CRASH_RESTART_DELAY = 5.seconds

        private const val MAX_LINES_BEFORE_JOINING = 5

        private val REGEX_NEW_LINE_REPLACE = Regex("\n+")

        private fun <T> T.conditionalTransform(condition: Boolean, transform: (T) -> T): T =
            if (condition) transform(this) else this
    }
}