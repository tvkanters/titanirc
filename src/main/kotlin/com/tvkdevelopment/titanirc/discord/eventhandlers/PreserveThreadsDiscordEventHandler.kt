package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.discord.eventhandlers.DiscordEventHandler.Registrar
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.common.entity.ArchiveDuration
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.threads.edit
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.event.guild.GuildCreateEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.hours

class PreserveThreadsDiscordEventHandler : DiscordEventHandler {

    private val jobs = mutableMapOf<Snowflake, Job>()

    override fun Registrar.register() {
        suspend fun ThreadChannel.preserveIfNeeded() {
            if (id.toString() in configuration.discordThreadsToPreserve) {
                jobs[id]?.cancel()
                jobs[id] = CoroutineScope(coroutineContext).launch {
                    while (true) {
                        Log.i("Activating thread: $name")
                        val archiveDuration = when (autoArchiveDuration) {
                            ArchiveDuration.Day -> ArchiveDuration.ThreeDays
                            else -> ArchiveDuration.Day
                        }
                        edit {
                            archived = false
                            autoArchiveDuration = archiveDuration
                        }
                        delay(REACTIVATE_INTERVAL)
                    }
                }
            }
        }

        on<GuildCreateEvent>({ guild.id }) {
            guild
                .activeThreads
                .collect {
                    it.preserveIfNeeded()
                }
        }
    }

    companion object {
        private val REACTIVATE_INTERVAL = 3.hours
    }
}