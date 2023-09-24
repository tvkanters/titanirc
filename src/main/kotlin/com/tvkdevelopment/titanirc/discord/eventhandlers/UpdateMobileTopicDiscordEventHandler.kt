package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.discord.eventhandlers.DiscordEventHandler.Registrar
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.core.entity.channel.TopGuildChannel
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.rest.json.request.ChannelModifyPatchRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.hours

/**
 * Aims to fix an issue on mobile clients (Android) where the topic shown is outdated if it was updated while the client
 * was offline.
 */
class UpdateMobileTopicDiscordEventHandler : DiscordEventHandler {

    private val jobs = mutableMapOf<Snowflake, Job>()

    override fun Registrar.register() {
        suspend fun TopGuildChannel.preserveIfNeeded() {
            if (id.toString() in configuration.discordChannelTopicsToPreserve) {
                jobs[id]?.cancel()
                jobs[id] = CoroutineScope(coroutineContext).launch {
                    while (true) {
                        Log.i("Refreshing topic: $name")
                        with (kord.rest.channel) {
                            val upToDateTopic = getChannel(id).topic.value
                            if (upToDateTopic != null) {
                                patchChannel(id, ChannelModifyPatchRequest(topic = Optional("$upToDateTopic ")))
                                patchChannel(id, ChannelModifyPatchRequest(topic = Optional(upToDateTopic)))
                            }
                        }
                        delay(TOPIC_UPDATE_INTERVAL)
                    }
                }
            }
        }

        on<GuildCreateEvent>({ guild.id }) {
            guild
                .channels
                .collect {
                    it.preserveIfNeeded()
                }
        }
    }

    companion object {
        private val TOPIC_UPDATE_INTERVAL = 1.hours
    }
}