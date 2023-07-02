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
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.rest.json.request.ChannelModifyPatchRequest
import kotlinx.coroutines.*


@OptIn(DelicateCoroutinesApi::class)
class Discord(
    private val configuration: TitanircConfiguration,
    private val topicRoles: TopicRoles,
) : BridgeClient {

    override val name = "Discord"

    private val scope = CoroutineScope(newSingleThreadContext("Discord"))
    private lateinit var kord: Kord

    private val messageListeners = mutableListOf<BridgeClient.MessageListener>()
    private val topicListeners = mutableListOf<BridgeClient.TopicListener>()

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

    private fun onKord(block: suspend Kord.() -> Unit) {
        scope.launch {
            if (::kord.isInitialized) {
                try {
                    block(kord)
                } catch (e: Exception) {
                    Log.e(e)
                }
            }
        }
    }

    private suspend fun Kord.sendMessage(channel: String, message: String) {
        getChannelOf<MessageChannel>(Snowflake(channel))
            ?.createMessage(message)
    }

    override fun relayMessage(channel: String, nick: String, message: String) {
        onKord {
            sendMessage(channel, "**<$nick>** $message")
        }
    }

    override fun addRelayMessageListener(listener: BridgeClient.MessageListener) {
        messageListeners += listener
    }

    override fun setTopic(channel: String, topic: String) {
        onKord {
            getChannelOf<MessageChannel>(Snowflake(channel))
                ?.takeIf { it.data.topic.value != topic }
                ?.apply { rest.channel.patchChannel(id, ChannelModifyPatchRequest(topic = Optional(topic))) }
        }
    }

    override fun addTopicListener(listener: BridgeClient.TopicListener) {
        topicListeners += listener
    }

    companion object {
        fun startBot(
            configuration: TitanircConfiguration,
            topicRoles: TopicRoles,
        ) =
            Discord(configuration, topicRoles).apply { startBot() }
    }
}