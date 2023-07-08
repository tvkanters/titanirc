package com.tvkdevelopment.titanirc.discord

import com.tvkdevelopment.titanirc.util.ifNull
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
    private val guildIdIndex = mutableMapOf<Snowflake, MutableGuildSnowflakeRegistry>()
    private val channelIndex = mutableMapOf<String, MutableGuildSnowflakeRegistry>()

    fun forGuild(guild: Guild): MutableGuildSnowflakeRegistry =
        guildSnowflakeRegistries.getOrPut(guild) {
            MutableGuildSnowflakeRegistry()
                .also { guildIdIndex[guild.id] = it }
        }

    fun forGuild(guildId: Snowflake): MutableGuildSnowflakeRegistry? =
        guildIdIndex[guildId]

    override fun forChannel(channel: String): GuildSnowflakeRegistry? =
        channelIndex[channel]
            .ifNull {
                guildSnowflakeRegistries
                    .entries
                    .firstNotNullOfOrNull { (guild, registry) ->
                        registry.takeIf { guild.channelIds.any { it.toString() == channel } }
                    }
                    ?.also { channelIndex[channel] = it }
            }
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
