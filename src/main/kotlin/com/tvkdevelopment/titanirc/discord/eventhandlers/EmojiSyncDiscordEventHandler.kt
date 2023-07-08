package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.discord.MutableSnowflakeRegistry
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.core.Kord
import dev.kord.core.event.guild.EmojisUpdateEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.on

class EmojiSyncDiscordEventHandler(
    private val mutableSnowflakeRegistry: MutableSnowflakeRegistry,
) : DiscordEventHandler {

    override fun Kord.register() {
        on<GuildCreateEvent> {
            mutableSnowflakeRegistry.forGuild(guild)
            guild.emojis.collect { emoji ->
                mutableSnowflakeRegistry.forGuild(emoji.guildId)?.emojiRegistry?.plusAssign(emoji)
            }
        }

        on<EmojisUpdateEvent> {
            Log.i("Emoji updated")
            mutableSnowflakeRegistry.forGuild(guildId)?.emojiRegistry
                ?.let { registry -> emojis.forEach { registry += it } }
        }
    }
}