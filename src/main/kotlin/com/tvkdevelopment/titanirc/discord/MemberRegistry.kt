package com.tvkdevelopment.titanirc.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member

interface MemberRegistry {
    fun getMembers(channel: String): Map<String, Snowflake>
}

class MutableMemberRegistry : MemberRegistry {
    private val members = mutableMapOf<Guild, MutableMap<String, Snowflake>>()

    override fun getMembers(channel: String): Map<String, Snowflake> =
        members.entries
            .firstOrNull { (guild, _) -> guild.channelIds.any { it.toString() == channel } }
            ?.value
            ?: emptyMap()

    fun add(guild: Guild, member: Member) {
        member.effectiveName
            .lowercase()
            .let { REGEX_NORMALIZE_MEMBER_NAME.matchAt(it, 0)?.groupValues?.get(1) }
            ?.takeIf { it.isNotEmpty() }
            ?.let { name ->
                members.getOrPut(guild) { mutableMapOf() }[name] = member.id
            }
    }

    companion object {
        private val REGEX_NORMALIZE_MEMBER_NAME = Regex("^([a-z0-9_-]+)")
    }
}