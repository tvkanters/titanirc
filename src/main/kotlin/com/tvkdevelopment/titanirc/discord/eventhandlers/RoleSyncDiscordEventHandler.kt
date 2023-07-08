package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.discord.MutableSnowflakeRegistry
import dev.kord.core.Kord
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.role.RoleCreateEvent
import dev.kord.core.event.role.RoleUpdateEvent
import dev.kord.core.on

class RoleSyncDiscordEventHandler(
    private val mutableSnowflakeRegistry: MutableSnowflakeRegistry,
) : DiscordEventHandler {

    override fun Kord.register() {
        on<GuildCreateEvent> {
            guild.roles.collect { role ->
                role.guild.asGuildOrNull()?.let { guild ->
                    mutableSnowflakeRegistry.forGuild(guild).roleRegistry += role
                }
            }
        }

        on<RoleCreateEvent> {
            mutableSnowflakeRegistry.forGuild(guildId)?.roleRegistry?.plusAssign(role)
        }

        on<RoleUpdateEvent> {
            mutableSnowflakeRegistry.forGuild(guildId)?.roleRegistry?.plusAssign(role)
        }
    }
}