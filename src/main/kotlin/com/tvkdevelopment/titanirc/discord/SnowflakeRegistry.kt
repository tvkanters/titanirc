package com.tvkdevelopment.titanirc.discord

import dev.kord.core.entity.Guild

interface SnowflakeRegistry {
    fun forChannel(channel: String): GuildSnowflakeRegistry?
}

class MutableSnowflakeRegistry : SnowflakeRegistry {
    private val guildSnowflakeRegistries = mutableMapOf<Guild, MutableGuildSnowflakeRegistry>()

    fun forGuild(guild: Guild): MutableGuildSnowflakeRegistry =
        guildSnowflakeRegistries.getOrPut(guild) { MutableGuildSnowflakeRegistry() }

    override fun forChannel(channel: String): GuildSnowflakeRegistry? =
        guildSnowflakeRegistries
            .entries
            .firstOrNull { (guild, _) -> guild.channelIds.any { it.toString() == channel } }
            ?.value
}

interface GuildSnowflakeRegistry {
    val memberRegistry: MemberRegistry
}

class MutableGuildSnowflakeRegistry: GuildSnowflakeRegistry {
    override val memberRegistry = MutableMemberRegistry()
}