package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.discord.MutableSnowflakeRegistry
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.core.Kord
import dev.kord.core.event.channel.ChannelCreateEvent
import dev.kord.core.event.channel.ChannelUpdateEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.on

class ChannelSyncDiscordEventHandler(
    private val mutableSnowflakeRegistry: MutableSnowflakeRegistry,
) : DiscordEventHandler {

    override fun Kord.register() {
        on<GuildCreateEvent> {
            guild.channels.collect { channel ->
                channel.getGuildOrNull()?.let { guild ->
                    mutableSnowflakeRegistry.forGuild(guild).channelRegistry += channel
                }
            }
        }

        on<ChannelCreateEvent> {
            Log.i("Discord channel created: ${channel.data.name.value}")
            channel.data.guildId.value
                ?.let { mutableSnowflakeRegistry.forGuild(it) }
                ?.channelRegistry
                ?.plusAssign(channel)
        }

        on<ChannelUpdateEvent> {
            Log.i("Discord channel updated: ${channel.data.name.value}")
            channel.data.guildId.value
                ?.let { mutableSnowflakeRegistry.forGuild(it) }
                ?.channelRegistry
                ?.plusAssign(channel)
        }
    }
}