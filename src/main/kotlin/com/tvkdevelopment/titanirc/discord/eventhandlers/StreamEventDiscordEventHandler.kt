package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.discord.eventhandlers.DiscordEventHandler.Registrar
import com.tvkdevelopment.titanirc.discord.topicValue
import com.tvkdevelopment.titanirc.util.Log
import com.tvkdevelopment.titanirc.util.StreamInfo
import com.tvkdevelopment.titanirc.util.Time
import com.tvkdevelopment.titanirc.util.TopicUtil
import dev.kord.common.entity.*
import dev.kord.common.entity.optional.Optional
import dev.kord.core.behavior.createScheduledEvent
import dev.kord.core.behavior.edit
import dev.kord.core.entity.GuildScheduledEvent
import dev.kord.core.entity.channel.Channel
import dev.kord.core.event.channel.ChannelUpdateEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.guild.GuildScheduledEventCreateEvent
import dev.kord.core.event.guild.GuildScheduledEventDeleteEvent
import dev.kord.core.event.guild.GuildScheduledEventUpdateEvent
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class StreamEventDiscordEventHandler : DiscordEventHandler {

    private val scheduledEventsById = mutableMapOf<Snowflake, GuildScheduledEvent>()
    private val streamInfoByGuild = mutableMapOf<Snowflake, StreamInfo?>()

    override fun Registrar.register() {

        suspend fun updateStreamEvent(guildId: Snowflake) {
            val guildEvents = scheduledEventsById.values.filter { it.guildId == guildId }
            val (streamScheduledEvents, externalEvents) = guildEvents.partition { it.isStreamEvent }

            val streamScheduledEvent = streamScheduledEvents.lastOrNull()
            val streamEvent = streamScheduledEvent?.toStreamEvent()
            val desiredStreamEvent =
                streamInfoByGuild[guildId]
                    .takeIf { externalEvents.none { it.status == GuildScheduledEventStatus.Active } }
                    ?.toStreamEvent()

            when {
                streamEvent == null && desiredStreamEvent != null -> {
                    Log.i("Creating stream event")
                    val currentTime = Time.currentTime
                    kord.getGuild(guildId).createScheduledEvent(
                        desiredStreamEvent.name,
                        GuildScheduledEventPrivacyLevel.GuildOnly,
                        currentTime + START_TIME_DELAY,
                        ScheduledEntityType.External
                    ) {
                        scheduledEndTime = currentTime + 12.hours
                        entityMetadata =
                            GuildScheduledEventEntityMetadata(location = Optional(desiredStreamEvent.location))
                    }
                }

                streamEvent != null && streamEvent == desiredStreamEvent && !streamScheduledEvent.isActive -> {
                    Log.i("Activating stream event")
                    streamScheduledEvent.edit {
                        status = GuildScheduledEventStatus.Active
                    }
                }

                streamEvent != null && desiredStreamEvent != null && streamEvent != desiredStreamEvent -> {
                    Log.i("Updating stream event")
                    streamScheduledEvent.edit {
                        name = desiredStreamEvent.name
                        entityMetadata =
                            GuildScheduledEventEntityMetadata(location = Optional(desiredStreamEvent.location))
                    }
                }

                streamEvent != null && desiredStreamEvent == null -> {
                    Log.i("Deleting stream event")
                    streamScheduledEvent.delete()
                }
            }
        }

        suspend fun onChannelUpdate(channel: Channel, old: Channel? = null) {
            val guildId = channel.data.guildId.value ?: return
            if (channel.id.toString() != configuration.discordEventSyncGuildToChannel[guildId.toString()]) {
                return
            }

            val topic = channel.topicValue.trim()
            if (old == null || topic != old.topicValue.trim()) {
                streamInfoByGuild[guildId] = TopicUtil.getStreamInfo(topic)
                updateStreamEvent(guildId)
            }
        }

        on<GuildCreateEvent>({ guild.id }) {
            guild.channels.collect(::onChannelUpdate)
        }

        onChannel<ChannelUpdateEvent>({ channel }) {
            onChannelUpdate(channel, old)
        }

        suspend fun updateScheduledEvent(guildId: Snowflake, id: Snowflake, scheduledEvent: GuildScheduledEvent?) {
            val event = scheduledEvent?.takeUnless { it.isFinished }
            if (event != null) {
                scheduledEventsById[id] = event
                updateStreamEvent(guildId)
            } else {
                if (scheduledEventsById.remove(id) != null) {
                    updateStreamEvent(guildId)
                }
            }
        }

        on<GuildScheduledEventCreateEvent>({ guildId }) {
            updateScheduledEvent(guildId, scheduledEventId, scheduledEvent)
        }

        on<GuildScheduledEventUpdateEvent>({ guildId }) {
            updateScheduledEvent(guildId, scheduledEventId, scheduledEvent)
        }

        on<GuildScheduledEventDeleteEvent>({ guildId }) {
            updateScheduledEvent(guildId, scheduledEventId, null)
        }
    }

    private val GuildScheduledEvent.isStreamEvent: Boolean
        get() = creatorId == kord.selfId

    private val GuildScheduledEvent.isFinished: Boolean
        get() = when (status) {
            GuildScheduledEventStatus.Scheduled,
            GuildScheduledEventStatus.Active,
            is GuildScheduledEventStatus.Unknown ->
                false

            GuildScheduledEventStatus.Completed,
            GuildScheduledEventStatus.Cancelled ->
                true
        }

    private val GuildScheduledEvent.isActive: Boolean
        get() = status == GuildScheduledEventStatus.Active

    companion object {

        private val START_TIME_DELAY = 1.minutes

        private data class StreamEvent(val name: String, val location: String)

        private fun GuildScheduledEvent.toStreamEvent() =
            StreamEvent(name, entityMetadata?.location?.value ?: "")

        private fun StreamInfo.toStreamEvent() =
            StreamEvent(title, streamer)
    }
}