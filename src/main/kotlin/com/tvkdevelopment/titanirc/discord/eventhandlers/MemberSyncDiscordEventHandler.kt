package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.discord.MutableSnowflakeRegistry
import com.tvkdevelopment.titanirc.discord.eventhandlers.DiscordEventHandler.Registrar
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.core.behavior.requestMembers
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.guild.MemberJoinEvent
import dev.kord.core.event.guild.MemberUpdateEvent
import dev.kord.gateway.PrivilegedIntent

@OptIn(PrivilegedIntent::class)
class MemberSyncDiscordEventHandler(
    private val mutableSnowflakeRegistry: MutableSnowflakeRegistry,
) : DiscordEventHandler {

    override fun Registrar.register() {
        on<GuildCreateEvent>({ guild.id }) {
            guild.requestMembers()
                .collect { event ->
                    event.members.forEach {
                        if (!it.isBot) {
                            it.getGuildOrNull()?.let { guild ->
                                mutableSnowflakeRegistry.forGuild(guild).memberRegistry += it
                            }
                        }
                    }
                }
        }

        on<MemberJoinEvent>({ member.guildId }) {
            Log.i("Discord member joined")
            mutableSnowflakeRegistry.forGuild(member.getGuild()).memberRegistry += member
        }

        on<MemberUpdateEvent>({ member.guildId }) {
            Log.i("Discord member updated")
            mutableSnowflakeRegistry.forGuild(member.getGuild()).memberRegistry += member
        }
    }
}