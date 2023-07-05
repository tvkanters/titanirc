package com.tvkdevelopment.titanirc.discord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member

interface MemberRegistry {
    val membersById: Map<Snowflake, MemberInfo>
    val membersByNormalizedName: Map<String, Snowflake>
}

class MutableMemberRegistry : MemberRegistry {
    private val index = MutableIndexedRegistry<Snowflake, MemberInfo, String> { it.normalizedName }

    override val membersByNormalizedName
        get() = index.itemsByNormalizedValue

    override val membersById: Map<Snowflake, MemberInfo> = index.itemsByKey

    operator fun plusAssign(member: Member) {
        index[member.id] = MemberInfo(member)
    }
}

data class MemberInfo(val effectiveName: String) {
    val normalizedName: String? =
        effectiveName
            .lowercase()
            .let { REGEX_NORMALIZE_NAME.matchAt(it, 0)?.groupValues?.get(1) }
            ?.takeIf { it.isNotEmpty() }

    constructor(member: Member) : this(member.effectiveName)

    companion object {
        private val REGEX_NORMALIZE_NAME = Regex("^([a-z0-9_-]+)")
    }
}
