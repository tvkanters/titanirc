package com.tvkdevelopment.titanirc.irc

import com.tvkdevelopment.titanirc.TitanircConfiguration
import com.tvkdevelopment.titanirc.bridge.BridgeClient
import com.tvkdevelopment.titanirc.irc.listeners.IrcBridgeListener
import com.tvkdevelopment.titanirc.irc.listeners.LogListener
import com.tvkdevelopment.titanirc.irc.listeners.NickFixListener
import com.tvkdevelopment.titanirc.irc.listeners.RestartListener
import com.tvkdevelopment.titanirc.util.splitMessageForIrc
import kotlinx.coroutines.*
import org.pircbotx.Configuration
import org.pircbotx.PircBotX
import org.pircbotx.delay.AdaptingDelay
import org.pircbotx.hooks.managers.SequentialListenerManager

@OptIn(DelicateCoroutinesApi::class)
class Irc(private val configuration: TitanircConfiguration) : BridgeClient {

    private val scope = CoroutineScope(newSingleThreadContext("Irc"))

    private lateinit var bot: PircBotX
    private var maxLineLength: Int = QUAKENET_MAXLINELENGTH

    private val messageListeners = mutableListOf<BridgeClient.MessageListener>()


    private fun startBot() {
        scope.launch {
            if (::bot.isInitialized) {
                bot.close()
            }
            bot = PircBotX(
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
                    addListener(RestartListener(60_000L, ::startBot))
                    addListener(NickFixListener(name))
                    addListener(IrcBridgeListener(messageListeners))
                }.buildConfiguration()
            )
            bot.startBot()
        }
    }

    override fun addRelayMessageListener(listener: BridgeClient.MessageListener) {
        messageListeners += listener
    }

    override fun relayMessage(channel: String, nick: String, message: String) {
        if (::bot.isInitialized) {
            bot.sendIRC().message(channel, message.splitMessageForIrc(maxLineLength, prefix = "<$nick> "))
        }
    }

    companion object {
        private const val QUAKENET_MAXLINELENGTH = 444

        fun connect(configuration: TitanircConfiguration) =
            Irc(configuration).apply { startBot() }
    }
}