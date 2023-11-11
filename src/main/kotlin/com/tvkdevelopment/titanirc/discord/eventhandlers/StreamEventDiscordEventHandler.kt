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
    private val streamInfoByChannelByGuild = mutableMapOf<Snowflake, Array<StreamInfo?>>()

    override fun Registrar.register() {

        suspend fun updateStreamEvent(guildId: Snowflake) {
            val guildEvents = scheduledEventsById.values.filter { it.guildId == guildId }
            val (streamScheduledEvents, externalEvents) = guildEvents.partition { it.isStreamEvent }

            val streamScheduledEvent = streamScheduledEvents.lastOrNull()
            val streamEvent = streamScheduledEvent?.toStreamEvent()
            val streamInfo = streamInfoByChannelByGuild[guildId]?.firstOrNull { it != null }

            val (currentSimilarExternalEvents, differentExternalEvents) =
                externalEvents.partition { streamInfo != null && it.isSimilarTo(streamInfo) && it.isNow }
            val desiredStreamEvent =
                streamInfo
                    .takeIf { differentExternalEvents.none { it.isActive } }
                    ?.toStreamEvent()

            streamScheduledEvents.dropLast(1).forEach { it.delete() }

            suspend fun deleteCurrentSimilarEvents() {
                currentSimilarExternalEvents.forEach { it.delete() }
            }

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
                    deleteCurrentSimilarEvents()
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
                    if (streamInfo == null) {
                        deleteCurrentSimilarEvents()
                    }
                }
            }
        }

        suspend fun onChannelUpdate(channel: Channel, old: Channel? = null) {
            val guildId = channel.data.guildId.value ?: return
            val channelString = channel.id.toString()
            val channelsToSync =
                configuration.discordEventSyncGuildToChannels.getOrDefault(guildId.toString(), emptyList())
            val channelIndex = channelsToSync.indexOfFirst { it.channel == channelString }
            if (channelIndex == -1) {
                return
            }

            val topic = channel.topicValue.trim()
            if (old == null || topic != old.topicValue.trim()) {
                val streamInfoByChannel =
                    streamInfoByChannelByGuild.getOrPut(guildId) { arrayOfNulls(channelsToSync.size) }
                streamInfoByChannel[channelIndex] =
                    TopicUtil.getStreamInfo(topic)
                        ?.let {
                            val label = channelsToSync[channelIndex].label
                            when {
                                label != null -> it.copy(streamer = "[${label}] ${it.streamer}")
                                else -> it
                            }
                        }
                updateStreamEvent(guildId)
            }
        }

        suspend fun onScheduledEventUpdate(
            guildId: Snowflake,
            id: Snowflake,
            scheduledEvent: GuildScheduledEvent?,
            updateStreamEvent: Boolean = true
        ) {
            val event = scheduledEvent?.takeUnless { it.isFinished }
            if (event != null) {
                scheduledEventsById[id] = event
                if (updateStreamEvent) {
                    updateStreamEvent(guildId)
                }

                if (scheduledEvent.location.trim().lowercase() in INVALID_STREAMERS) {
                    val newStreamer = scheduledEvent.creatorId
                        ?.let { scheduledEvent.getGuild().getMemberOrNull(it)?.effectiveName }
                    if (newStreamer != null) {
                        Log.i("Updating invalid stream event streamer")
                        scheduledEvent.edit {
                            entityMetadata = GuildScheduledEventEntityMetadata(location = Optional(newStreamer))
                        }
                    }
                }
            } else {
                if (scheduledEventsById.remove(id) != null && updateStreamEvent) {
                    updateStreamEvent(guildId)
                }
            }
        }

        on<GuildCreateEvent>({ guild.id }) {
            guild.scheduledEvents.collect { onScheduledEventUpdate(guild.id, it.id, it, updateStreamEvent = false) }
            guild.channels.collect(::onChannelUpdate)
        }

        onChannel<ChannelUpdateEvent>({ channel }) {
            onChannelUpdate(channel, old)
        }

        on<GuildScheduledEventCreateEvent>({ guildId }) {
            onScheduledEventUpdate(guildId, scheduledEventId, scheduledEvent)
        }

        on<GuildScheduledEventUpdateEvent>({ guildId }) {
            onScheduledEventUpdate(guildId, scheduledEventId, scheduledEvent)
        }

        on<GuildScheduledEventDeleteEvent>({ guildId }) {
            onScheduledEventUpdate(guildId, scheduledEventId, null)
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

    companion object {

        private val START_TIME_DELAY = 1.minutes
        private val STREAM_RELEVANCE_SLOP = 15.minutes

        private val INVALID_STREAMERS = setOf("stream", "dopelives")

        private data class StreamEvent(val name: String, val location: String)

        private fun StreamInfo.toStreamEvent() =
            StreamEvent(title, streamer)

        private fun GuildScheduledEvent.toStreamEvent() =
            StreamEvent(name, location)

        private val GuildScheduledEvent.location: String
            get() = entityMetadata?.location?.value ?: ""

        private fun GuildScheduledEvent.isSimilarTo(other: StreamInfo): Boolean =
            location.equals(other.streamer, ignoreCase = true)

        private val GuildScheduledEvent.isActive: Boolean
            get() = status == GuildScheduledEventStatus.Active

        private val GuildScheduledEvent.isNow: Boolean
            get() = isActive || scheduledStartTime - STREAM_RELEVANCE_SLOP <= Time.currentTime
    }
}