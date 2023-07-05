package com.tvkdevelopment.titanirc.discord

import com.tvkdevelopment.titanirc.TitanircConfiguration
import com.tvkdevelopment.titanirc.bridge.BridgeClient
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.requestMembers
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.Event
import dev.kord.core.event.channel.ChannelUpdateEvent
import dev.kord.core.event.gateway.DisconnectEvent
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.gateway.ResumedEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.guild.MemberUpdateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.DefaultGateway
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.gateway.ratelimit.IdentifyRateLimiter
import dev.kord.rest.json.request.ChannelModifyPatchRequest
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds


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
    private val slashMeListeners = mutableListOf<BridgeClient.SlashMeListener>()
    private val topicListeners = mutableListOf<BridgeClient.TopicListener>()

    private val mutableSnowflakeRegistry = MutableSnowflakeRegistry()
    val snowflakeRegistry: SnowflakeRegistry = mutableSnowflakeRegistry

    @OptIn(PrivilegedIntent::class)
    override fun connect() {
        scope.launch {
            Kord(configuration.discordToken) {
                gateways { resources, shards ->
                    shards.map {
                        DefaultGateway {
                            client = resources.httpClient
                            identifyRateLimiter = IdentifyRateLimiter(resources.maxConcurrency, defaultDispatcher)
                            reconnectRetry = IncrementalRetry(2.seconds, 2.seconds, 30.seconds)
                        }
                    }
                }
            }.apply {
                on<Event> {
                    Log.i("Event: $this")
                }

                on<ReadyEvent> {
                    Log.i("Discord ready")
                    bot = kord
                }

                on<DisconnectEvent> {
                    Log.i("Discord disconnected: ${this::class.simpleName}")
                    bot = null
                }

                on<ResumedEvent> {
                    Log.i("Discord resumed")
                    bot = kord
                }

                on<GuildCreateEvent> {
                    Log.i("Discord server joined: ${guild.data.name}")
                    guild.editSelfNickname(nick)
                    guild
                        .requestMembers()
                        .collect { event ->
                            Log.i("Discord member chunk added")
                            event.members.forEach {
                                if (!it.isBot) {
                                    mutableSnowflakeRegistry.forGuild(guild).memberRegistry += it
                                }
                            }
                        }
                }

                on<MemberJoinEvent> {
                    Log.i("Discord member joined")
                    mutableSnowflakeRegistry.forGuild(member.getGuild()).memberRegistry += member
                }

                on<MemberUpdateEvent> {
                    Log.i("Discord member updated")
                    mutableSnowflakeRegistry.forGuild(member.getGuild()).memberRegistry += member
                }

                on<MessageCreateEvent> {
                    member
                        ?.takeUnless { it.isBot }
                        ?.let { member ->
                            val attachedUrls =
                                message.attachments.map { it.url }
                                    .filterNot { message.content.contains(it) }
                            val messageToSend =
                                listOf(message.content)
                                    .plus(attachedUrls)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" ")
                            messageListeners.forEach {
                                it.onMessage(message.channel.id.toString(), member.effectiveName, messageToSend)
                            }
                        }
                }

                on<ChannelUpdateEvent> {
                    val topic = channel.topicValue
                    if (topic != old.topicValue) {
                        val channelString = channel.id.toString()
                        if (old != null && topic.isNotBlank()) {
                            val topicRole = topicRoles.getRole(channelString, topic)
                            channel.asChannelOfOrNull<MessageChannel>()
                                ?.createMessage(":bell: ${topicRole?.let { "<@&$it>" } ?: "Topic"} updated: $topic")
                        }
                        topicListeners.forEach { it.onTopicChanged(channelString, topic) }
                    }
                }

                login {
                    intents += Intent.MessageContent
                    intents += Intent.GuildMembers
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

    override fun relaySlashMe(channel: String, nick: String, message: String) {
        onBot {
            sendMessage(channel, "\\* $nick $message")
        }
    }

    override fun addRelaySlashMeListener(listener: BridgeClient.SlashMeListener) {
        slashMeListeners += listener
    }

    override fun setTopic(channel: String, topic: String) {
        onBot {
            getChannelOf<MessageChannel>(Snowflake(channel))
                ?.takeIf { it.topicValue != topic }
                ?.apply { rest.channel.patchChannel(id, ChannelModifyPatchRequest(topic = Optional(topic))) }
        }
    }

    override fun addTopicListener(listener: BridgeClient.TopicListener) {
        topicListeners += listener
    }

    companion object {
        private val dev.kord.core.entity.channel.Channel?.topicValue: String
            get() = this?.data?.topic?.value ?: ""
    }
}