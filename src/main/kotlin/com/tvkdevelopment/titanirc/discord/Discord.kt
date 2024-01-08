package com.tvkdevelopment.titanirc.discord

import com.tvkdevelopment.titanirc.TitanircConfiguration
import com.tvkdevelopment.titanirc.bridge.BridgeClient
import com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations.escapeDiscordFormatting
import com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations.unescapeDiscordFormatting
import com.tvkdevelopment.titanirc.discord.eventhandlers.*
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.core.Kord
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.Event
import dev.kord.core.event.gateway.DisconnectEvent
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.gateway.ResumedEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.on
import dev.kord.gateway.DefaultGateway
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kord.gateway.ratelimit.IdentifyRateLimiter
import dev.kord.rest.json.request.ChannelModifyPatchRequest
import kotlinx.coroutines.*
import kotlin.properties.Delegates.observable
import kotlin.time.Duration.Companion.seconds


@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
class Discord(
    private val configuration: TitanircConfiguration,
) : BridgeClient {

    override val name = "Discord"

    private val scope = CoroutineScope(newSingleThreadContext("Discord"))

    private var kordJob by observable<Job?>(null) { _, oldValue, _ ->
        oldValue?.cancel()
    }

    private var zombieReconnectJob by observable<Job?>(null) { _, oldValue, _ ->
        oldValue?.cancel()
    }

    private var bot by observable<Kord?>(null) { _, _, newValue ->
        if (newValue != null) {
            zombieReconnectJob = null
        }
    }

    private val bridgeListeners = mutableListOf<BridgeClient.Listener>()

    private val mutableSnowflakeRegistry = MutableSnowflakeRegistry()
    val snowflakeRegistry: SnowflakeRegistry = mutableSnowflakeRegistry

    private val eventHandlers = DiscordEventHandlers(
        BridgeDiscordEventHandler(bridgeListeners, snowflakeRegistry),
        MemberSyncDiscordEventHandler(mutableSnowflakeRegistry),
        ChannelSyncDiscordEventHandler(mutableSnowflakeRegistry),
        EmojiSyncDiscordEventHandler(mutableSnowflakeRegistry),
        RoleSyncDiscordEventHandler(mutableSnowflakeRegistry),
        PreserveThreadsDiscordEventHandler(),
        UpdateMobileTopicDiscordEventHandler(),
        StreamEventDiscordEventHandler(),
    )

    @OptIn(PrivilegedIntent::class)
    override fun connect() {
        kordJob = scope.launch {
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
                if (configuration.isDevEnv) {
                    on<Event> {
                        Log.i("Event: $this")
                    }
                }

                on<ReadyEvent> {
                    Log.i("Discord ready")
                    bot = kord
                }

                on<DisconnectEvent> {
                    if (this !is DisconnectEvent.RetryLimitReachedEvent) {
                        Log.i("Discord disconnected: ${this::class.simpleName}")
                    }
                    bot = null

                    if (this is DisconnectEvent.ZombieConnectionEvent && zombieReconnectJob == null) {
                        zombieReconnectJob = scope.launch {
                            while (bot == null) {
                                kordJob = null
                                delay(ZOMBIE_RECONNECT_DELAY)
                                Log.i("Trying to reconnect zombie connection")
                                connect()
                            }
                        }
                    }
                }

                on<ResumedEvent> {
                    Log.i("Discord resumed")
                    bot = kord
                }

                on<GuildCreateEvent> {
                    Log.i("Discord server joined: ${guild.data.name}")
                    guild.editSelfNickname(configuration.discordNick)
                }

                eventHandlers.register(this, configuration)

                login {
                    intents += Intent.MessageContent
                    intents += Intent.GuildMembers
                }
            }
        }
    }

    override fun addBridgeListener(listener: BridgeClient.Listener) {
        bridgeListeners += listener
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
            sendMessage(channel, "**<${nick.escapeDiscordFormatting()}>** $message")
        }
    }

    override fun relaySlashMe(channel: String, nick: String, message: String) {
        onBot {
            sendMessage(channel, "\\* ${nick.escapeDiscordFormatting()} $message")
        }
    }

    override fun setTopic(channel: String, topic: String) {
        onBot {
            getChannelOf<MessageChannel>(Snowflake(channel))
                ?.takeIf { it.topicValue.unescapeDiscordFormatting() != topic.unescapeDiscordFormatting() }
                ?.apply { rest.channel.patchChannel(id, ChannelModifyPatchRequest(topic = Optional(topic))) }
        }
    }

    companion object {
        private val ZOMBIE_RECONNECT_DELAY = 10.seconds
    }
}