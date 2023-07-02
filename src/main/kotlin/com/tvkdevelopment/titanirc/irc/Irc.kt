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

@OptIn(DelicateCoroutinesApi::class)
class Irc(private val configuration: TitanircConfiguration) : BridgeClient {

    override val name = "IRC"

    private val scope = CoroutineScope(newSingleThreadContext("Irc"))

    private var bot: PircBotX? = null
    private var maxLineLength: Int = QUAKENET_MAXLINELENGTH

    private val messageListeners = mutableListOf<BridgeClient.MessageListener>()
    private val topicListeners = mutableListOf<BridgeClient.TopicListener>()

    override fun connect() {
        scope.launch {
            bot?.close()
            PircBotX(
                Configuration.Builder().apply {
                    name = configuration.ircNick
                    login = configuration.ircUsername
                    realName = "Dopelives bridge"
                    configuration.ircPassword?.let {
                        nickservCustomMessage = "PRIVMSG Q@CServe.quakenet.org :AUTH ${configuration.ircUsername} $it"
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
                    autoReconnectAttempts = 100
                    autoReconnectDelay = AdaptingDelay(2_000L, 30_000L).also { addListener(it) }
                    isUserModeHideRealHost = true
                    isAutoNickChange = true

                    setListenerManager(SequentialListenerManager.newDefault())

                    addListener(LogListener())
                    addListener(RestartListener(60_000L, ::connect))
                    addListener(NickFixListener(name))
                    addListener(IrcBridgeListener(messageListeners, topicListeners))
                }.buildConfiguration()
            )
                .also { bot = it }
                .startBot()
        }
    }

    private fun onBot(block: PircBotX.() -> Unit) {
        try {
            bot?.block()
        } catch (e: Exception) {
            Log.e(e)
        }
    }

    override fun relayMessage(channel: String, nick: String, message: String) {
        onBot {
            sendIRC().message(
                channel,
                message.splitMessageForIrc(maxLineLength, prefix = "<$nick> ")
            )
        }
    }

    override fun addRelayMessageListener(listener: BridgeClient.MessageListener) {
        messageListeners += listener
    }

    override fun setTopic(channel: String, topic: String) {
        onBot {
            userChannelDao.getChannel(channel)
                .takeIf { it.topic != topic }
                ?.apply { send().setTopic(topic) }
        }
    }

    override fun addTopicListener(listener: BridgeClient.TopicListener) {
        topicListeners += listener
    }

    companion object {
        private const val QUAKENET_MAXLINELENGTH = 443
    }
}