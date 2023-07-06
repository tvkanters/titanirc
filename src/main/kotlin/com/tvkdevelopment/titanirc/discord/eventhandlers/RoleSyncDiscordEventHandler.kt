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
            val registry = mutableSnowflakeRegistry.forGuild(guild).roleRegistry
            guild.roles.collect { registry += it }
        }

        on<RoleCreateEvent> {
            mutableSnowflakeRegistry.forGuild(guildId)?.roleRegistry?.plusAssign(role)
        }

        on<RoleUpdateEvent> {
            mutableSnowflakeRegistry.forGuild(guildId)?.roleRegistry?.plusAssign(role)
        }
    }
}