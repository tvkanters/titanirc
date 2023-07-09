package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.discord.MutableSnowflakeRegistry
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.role.RoleCreateEvent
import dev.kord.core.event.role.RoleUpdateEvent

class RoleSyncDiscordEventHandler(
    private val mutableSnowflakeRegistry: MutableSnowflakeRegistry,
) : DiscordEventHandler {

    override fun DiscordEventHandler.Registrar.register() {
        on<GuildCreateEvent>({ guild.id }) {
            guild.roles.collect { role ->
                role.guild.asGuildOrNull()?.let { guild ->
                    mutableSnowflakeRegistry.forGuild(guild).roleRegistry += role
                }
            }
        }

        on<RoleCreateEvent>({ guildId }) {
            mutableSnowflakeRegistry.forGuild(guildId)?.roleRegistry?.plusAssign(role)
        }

        on<RoleUpdateEvent>({ guildId }) {
            mutableSnowflakeRegistry.forGuild(guildId)?.roleRegistry?.plusAssign(role)
        }
    }
}