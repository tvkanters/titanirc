package com.tvkdevelopment.titanirc.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member

interface MemberRegistry {
    val membersById: Map<Snowflake, MemberInfo>
    val membersByNormalizedName: Map<String, Snowflake>
}

class MutableMemberRegistry : MemberRegistry {
    private val mutableMembersById = mutableMapOf<Snowflake, MemberInfo>()
    override val membersByNormalizedName = mutableMapOf<String, Snowflake>()
        get() {
            if (membersByNameInvalidated) {
                mutableMembersById.forEach { (id, info) ->
                    info.normalizedName?.let { field[it] = id }
                }
                membersByNameInvalidated = false
            }
            return field
        }
    private var membersByNameInvalidated = false

    override val membersById: Map<Snowflake, MemberInfo> = mutableMembersById

    operator fun plusAssign(member: Member) {
        mutableMembersById[member.id] = MemberInfo(member)
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
