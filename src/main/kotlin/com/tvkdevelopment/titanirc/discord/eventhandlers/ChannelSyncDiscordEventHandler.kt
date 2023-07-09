package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.discord.MutableSnowflakeRegistry
import com.tvkdevelopment.titanirc.discord.eventhandlers.DiscordEventHandler.Registrar
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.core.event.channel.ChannelCreateEvent
import dev.kord.core.event.channel.ChannelUpdateEvent
import dev.kord.core.event.guild.GuildCreateEvent

class ChannelSyncDiscordEventHandler(
    private val mutableSnowflakeRegistry: MutableSnowflakeRegistry,
) : DiscordEventHandler {

    override fun Registrar.register() {
        on<GuildCreateEvent>({ guild.id }) {
            guild.channels.collect { channel ->
                channel.getGuildOrNull()?.let { guild ->
                    mutableSnowflakeRegistry.forGuild(guild).channelRegistry += channel
                }
            }
        }

        onChannel<ChannelCreateEvent>({ channel }) {
            Log.i("Discord channel created: ${channel.data.name.value}")
            channel.data.guildId.value
                ?.let { mutableSnowflakeRegistry.forGuild(it) }
                ?.channelRegistry
                ?.plusAssign(channel)
        }

        onChannel<ChannelUpdateEvent>({ channel }) {
            Log.i("Discord channel updated: ${channel.data.name.value}")
            channel.data.guildId.value
                ?.let { mutableSnowflakeRegistry.forGuild(it) }
                ?.channelRegistry
                ?.plusAssign(channel)
        }
    }
}