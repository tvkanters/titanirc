package com.tvkdevelopment.titanirc.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.Channel

typealias MemberRegistry = ItemIndexRegistry<Snowflake, ItemInfo, String>
typealias ChannelRegistry = ItemIndexRegistry<Snowflake, ItemInfo, String>
typealias RoleRegistry = ItemIndexRegistry<Snowflake, ItemInfo, String>
typealias EmojiRegistry = ItemIndexRegistry<Snowflake, ItemInfo, String>

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
    val roleRegistry: RoleRegistry
    val emojiRegistry: EmojiRegistry
}

class MutableGuildSnowflakeRegistry : GuildSnowflakeRegistry {
    override val memberRegistry = MutableEntityItemRegistry<Member> { it.effectiveName }
    override val channelRegistry = MutableEntityItemRegistry<Channel> { it.data.name.value }
    override val roleRegistry = MutableEntityItemRegistry<Role> { it.name }
    override val emojiRegistry = MutableEntityItemRegistry<GuildEmoji> { it.name }
}
