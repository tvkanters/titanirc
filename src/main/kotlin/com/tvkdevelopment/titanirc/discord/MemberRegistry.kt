package com.tvkdevelopment.titanirc.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member

interface MemberRegistry {
    fun getMembersById(channel: String): Map<Snowflake, MemberInfo>
    fun getMembersByNormalizedName(channel: String): Map<String, Snowflake>
}

class MutableMemberRegistry : MemberRegistry {
    private val membersById = mutableMapOf<Guild, MutableMap<Snowflake, MemberInfo>>()
    private val membersByNormalizedName = mutableMapOf<Guild, MutableMap<String, Snowflake>>()
        get() {
            if (membersByNameInvalidated) {
                membersById.forEach { (guild, member) ->
                    val guildMemberIndex = field.getOrPut(guild) { mutableMapOf() }
                    member.forEach { (id, info) ->
                        info.normalizedName?.let { guildMemberIndex[it] = id }
                    }
                }
                membersByNameInvalidated = false
            }
            return field
        }
    private var membersByNameInvalidated = false

    override fun getMembersById(channel: String): Map<Snowflake, MemberInfo> =
        membersById.forChannel(channel) ?: emptyMap()

    override fun getMembersByNormalizedName(channel: String): Map<String, Snowflake> =
        membersByNormalizedName.forChannel(channel) ?: emptyMap()

    private fun <V> Map<Guild, V>.forChannel(channel: String): V? =
        entries
            .firstOrNull { (guild, _) -> guild.channelIds.any { it.toString() == channel } }
            ?.value

    fun add(guild: Guild, member: Member) {
        membersById.getOrPut(guild) { mutableMapOf() }[member.id] = MemberInfo(member)
        membersByNameInvalidated = true
    }
}

data class MemberInfo(val effectiveName: String) {
    val normalizedName: String? =
        effectiveName
            .lowercase()
            .let { REGEX_NORMALIZE_MEMBER_NAME.matchAt(it, 0)?.groupValues?.get(1) }
            ?.takeIf { it.isNotEmpty() }

    constructor(member: Member) : this(member.effectiveName)

    companion object {
        private val REGEX_NORMALIZE_MEMBER_NAME = Regex("^([a-z0-9_-]+)")
    }
}
