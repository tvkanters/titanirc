package com.tvkdevelopment.titanirc.discord

import com.tvkdevelopment.titanirc.TitanircConfiguration
import com.tvkdevelopment.titanirc.bridge.BridgeClient
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.channel.ChannelUpdateEvent
import dev.kord.core.event.gateway.DisconnectEvent
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.json.request.ChannelModifyPatchRequest
import dev.kord.rest.json.request.CurrentUserNicknameModifyRequest
import kotlinx.coroutines.*


@OptIn(DelicateCoroutinesApi::class)
class Discord(
    private val configuration: TitanircConfiguration,
    private val nick: String,
    private val topicRoles: TopicRoles,
) : BridgeClient {

    override val name = "Discord"

    private val scope = CoroutineScope(newSingleThreadContext("Discord"))
    private var bot: Kord? = null

    private val messageListeners = mutableListOf<BridgeClient.MessageListener>()
    private val topicListeners = mutableListOf<BridgeClient.TopicListener>()

    override fun connect() {
        scope.launch {
            with(Kord(configuration.discordToken)) {
                on<ReadyEvent> {
                    Log.i("Discord connected")
                    bot = kord
                }

                on<DisconnectEvent> {
                    Log.i("Discord disconnected")
                    bot = null
                }

                on<GuildCreateEvent> {
                    Log.i("Discord server joined: ${guild.data.name}")
                    rest.guild.modifyCurrentUserNickname(
                        guild.id,
                        CurrentUserNicknameModifyRequest(Optional(nick))
                    )
                }

                on<MessageCreateEvent> {
                    member
                        ?.takeUnless { it.isBot }
                        ?.let { member ->
                            messageListeners.forEach {
                                it.onMessage(message.channel.id.toString(), member.displayName, message.content)
                            }
                        }
                }

                on<ChannelUpdateEvent> {
                    val topic = channel.data.topic.value ?: ""
                    val channelString = channel.id.toString()
                    if (old != null) {
                        val topicRole = topicRoles.getRole(channelString, topic)
                        channel.asChannelOfOrNull<MessageChannel>()
                            ?.createMessage(":bell: ${topicRole?.let { "<@&$it>" } ?: "Topic"} updated: $topic")
                    }
                    topicListeners.forEach { it.onTopicChanged(channelString, topic) }
                }

                login {
                    @OptIn(PrivilegedIntent::class)
                    intents += Intent.MessageContent
                }
            }
        }
    }

    private fun onBot(block: suspend Kord.() -> Unit) {
        scope.launch {
            try {
                bot?.block()
            } catch (e: Exception) {
                Log.e(e)
            }
        }
    }

    private suspend fun Kord.sendMessage(channel: String, message: String) {
        getChannelOf<MessageChannel>(Snowflake(channel))
            ?.createMessage(message)
    }

    override fun relayMessage(channel: String, nick: String, message: String) {
        onBot {
            sendMessage(channel, "**<$nick>** $message")
        }
    }

    override fun addRelayMessageListener(listener: BridgeClient.MessageListener) {
        messageListeners += listener
    }

    override fun setTopic(channel: String, topic: String) {
        onBot {
            getChannelOf<MessageChannel>(Snowflake(channel))
                ?.takeIf { it.data.topic.value != topic }
                ?.apply { rest.channel.patchChannel(id, ChannelModifyPatchRequest(topic = Optional(topic))) }
        }
    }

    override fun addTopicListener(listener: BridgeClient.TopicListener) {
        topicListeners += listener
    }
}