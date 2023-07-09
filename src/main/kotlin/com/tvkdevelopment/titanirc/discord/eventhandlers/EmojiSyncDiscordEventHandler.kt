package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.discord.MutableSnowflakeRegistry
import com.tvkdevelopment.titanirc.discord.eventhandlers.DiscordEventHandler.Registrar
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.core.event.guild.EmojisUpdateEvent
import dev.kord.core.event.guild.GuildCreateEvent

class EmojiSyncDiscordEventHandler(
    private val mutableSnowflakeRegistry: MutableSnowflakeRegistry,
) : DiscordEventHandler {

    override fun Registrar.register() {
        on<GuildCreateEvent>({ guild.id }) {
            mutableSnowflakeRegistry.forGuild(guild)
            guild.emojis.collect { emoji ->
                mutableSnowflakeRegistry.forGuild(emoji.guildId)?.emojiRegistry?.plusAssign(emoji)
            }
        }

        on<EmojisUpdateEvent>({ guildId }) {
            Log.i("Emoji updated")
            mutableSnowflakeRegistry.forGuild(guildId)?.emojiRegistry
                ?.let { registry -> emojis.forEach { registry += it } }
        }
    }
}