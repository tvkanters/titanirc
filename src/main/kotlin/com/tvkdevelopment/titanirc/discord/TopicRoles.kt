package com.tvkdevelopment.titanirc.discord

import dev.kord.common.entity.Snowflake

typealias GetTopicRoleForTopic = (topic: String) -> ULong?

class TopicRoles(private val channelToRoleMap: Map<String, GetTopicRoleForTopic>) {
    constructor(vararg channelsToRoles: Pair<String, GetTopicRoleForTopic>) : this(channelsToRoles.toMap())

    fun getRole(channel: String, topic: String): Snowflake? =
        channelToRoleMap[channel]?.invoke(topic)?.let { Snowflake(it) }
}