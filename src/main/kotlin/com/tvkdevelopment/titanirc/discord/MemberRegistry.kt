package com.tvkdevelopment.titanirc.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member

interface MemberRegistry {
    fun getMembers(channel: String): Map<String, Snowflake>
}

class MutableMemberRegistry : MemberRegistry {
    private val members = mutableMapOf<Guild, MutableMap<Snowflake, String>>()
    private val memberIndex = mutableMapOf<Guild, MutableMap<String, Snowflake>>()
        get() {
            if (memberIndexInvalidated) {
                members.forEach { (guild, member) ->
                    val guildMemberIndex = field.getOrPut(guild) { mutableMapOf() }
                    member.forEach { (id, name) ->
                        guildMemberIndex[name] = id
                    }
                }
                memberIndexInvalidated = false
            }
            return field
        }
    private var memberIndexInvalidated = false

    override fun getMembers(channel: String): Map<String, Snowflake> =
        memberIndex.entries
            .firstOrNull { (guild, _) -> guild.channelIds.any { it.toString() == channel } }
            ?.value
            ?: emptyMap()

    fun add(guild: Guild, member: Member) {
        member.effectiveName
            .lowercase()
            .let { REGEX_NORMALIZE_MEMBER_NAME.matchAt(it, 0)?.groupValues?.get(1) }
            ?.takeIf { it.isNotEmpty() }
            ?.let { name ->
                members.getOrPut(guild) { mutableMapOf() }[member.id] = name
                memberIndexInvalidated = true
            }
    }

    companion object {
        private val REGEX_NORMALIZE_MEMBER_NAME = Regex("^([a-z0-9_-]+)")
    }
}