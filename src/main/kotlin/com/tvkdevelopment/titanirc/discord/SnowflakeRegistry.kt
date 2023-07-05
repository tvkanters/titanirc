package com.tvkdevelopment.titanirc.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild

interface SnowflakeRegistry {
    fun forChannel(channel: String): GuildSnowflakeRegistry?
}

class MutableSnowflakeRegistry : SnowflakeRegistry {
    private val guildSnowflakeRegistries = mutableMapOf<Guild, MutableGuildSnowflakeRegistry>()

    fun forGuild(guild: Guild): MutableGuildSnowflakeRegistry =
        guildSnowflakeRegistries.getOrPut(guild) { MutableGuildSnowflakeRegistry() }

    fun forGuild(guildId: Snowflake): MutableGuildSnowflakeRegistry? =
        guildSnowflakeRegistries
            .entries
            .firstOrNull { (guild, _) -> guild.id == guildId }
            ?.value

    override fun forChannel(channel: String): GuildSnowflakeRegistry? =
        guildSnowflakeRegistries
            .entries
            .firstOrNull { (guild, _) -> guild.channelIds.any { it.toString() == channel } }
            ?.value
}

interface GuildSnowflakeRegistry {
    val memberRegistry: MemberRegistry
    val channelRegistry: ChannelRegistry
}

class MutableGuildSnowflakeRegistry: GuildSnowflakeRegistry {
    override val memberRegistry = MutableMemberRegistry()
    override val channelRegistry = MutableChannelRegistry()
}