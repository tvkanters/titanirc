package com.tvkdevelopment.titanirc.discord

import com.tvkdevelopment.titanirc.TitanircConfiguration
import com.tvkdevelopment.titanirc.bridge.BridgeClient
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.common.entity.Snowflake
import dev.kord.common.exception.RequestException
import dev.kord.core.Kord
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.*


@OptIn(DelicateCoroutinesApi::class)
class Discord(private val configuration: TitanircConfiguration) : BridgeClient {

    private val scope = CoroutineScope(newSingleThreadContext("Discord"))
    private lateinit var kord: Kord

    private val messageListeners = mutableListOf<BridgeClient.MessageListener>()

    private fun startBot() {
        scope.launch {
            kord = Kord(configuration.discordToken)
            with(kord) {
                on<MessageCreateEvent> {
                    member
                        ?.takeUnless { it.isBot }
                        ?.let { member ->
                            messageListeners.forEach {
                                it.onMessage(message.channel.id.toString(), member.displayName, message.content)
                            }
                        }
                }

                login {
                    @OptIn(PrivilegedIntent::class)
                    intents += Intent.MessageContent
                }
            }
        }
    }

    override fun addRelayMessageListener(listener: BridgeClient.MessageListener) {
        messageListeners += listener
    }

    override fun relayMessage(channel: String, nick: String, message: String) {
        scope.launch {
            if (::kord.isInitialized) {
                try {
                    kord.getChannelOf<MessageChannel>(Snowflake(channel))
                        ?.createMessage("**<$nick>** $message")
                } catch (e: RequestException) {
                    Log.e(e)
                }
            }
        }
    }

    companion object {
        fun startBot(configuration: TitanircConfiguration) =
            Discord(configuration).apply { startBot() }
    }
}