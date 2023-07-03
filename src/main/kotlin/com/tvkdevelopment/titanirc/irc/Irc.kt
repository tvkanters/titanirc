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

@OptIn(DelicateCoroutinesApi::class)
class Irc(private val configuration: TitanircConfiguration) : BridgeClient {

    override val name = "IRC"

    private val scope = CoroutineScope(newSingleThreadContext("Irc"))
    private var connectJob: Job? = null
    private val messageSender = IrcMessageSender(configuration, ::bot)

    private var bot: PircBotX? = null
    private var maxLineLength: Int = QUAKENET_MAXLINELENGTH

    private val messageListeners = mutableListOf<BridgeClient.MessageListener>()
    private val slashMeListeners = mutableListOf<BridgeClient.SlashMeListener>()
    private val topicListeners = mutableListOf<BridgeClient.TopicListener>()

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
                    when {
                        configuration.isDevEnv -> {
                            addAutoJoinChannel("#titanirc")
                        }

                        else -> {
                            addAutoJoinChannel("#dopefish_lives")
                            addAutoJoinChannel("#freamonsmind")
                            addAutoJoinChannel("#dopefish_gdq")
                        }
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
                    addListener(IrcBridgeListener(messageListeners, slashMeListeners, topicListeners))
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

    override fun relayMessage(channel: String, nick: String, message: String) {
        sendMessage(channel, "<$nick> ", message)
    }

    override fun addRelayMessageListener(listener: BridgeClient.MessageListener) {
        messageListeners += listener
    }

    override fun relaySlashMe(channel: String, nick: String, message: String) {
        sendMessage(channel, "* $nick ", message)
    }

    override fun addRelaySlashMeListener(listener: BridgeClient.SlashMeListener) {
        slashMeListeners += listener
    }

    private fun sendMessage(channel: String, prefix: String, message: String) {
        message.splitMessageForIrc(maxLineLength, prefix = prefix)
            .forEach { messageSender.sendMessage(channel, it) }
    }

    override fun setTopic(channel: String, topic: String) {
        messageSender.setTopic(channel, topic)
    }

    override fun addTopicListener(listener: BridgeClient.TopicListener) {
        topicListeners += listener
    }

    companion object {
        private const val QUAKENET_MAXLINELENGTH = 443
        private val DISCONNECT_RESTART_DELAY = 30.seconds
        private val CRASH_RESTART_DELAY = 5.seconds
    }
}