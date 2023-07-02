package com.tvkdevelopment.titanirc.discord

typealias GetTopicRoleForTopic = (topic: String) -> String?

class TopicRoles(private val channelToRoleMap: Map<String, GetTopicRoleForTopic>) {
    constructor(vararg channelsToRoles: Pair<String, GetTopicRoleForTopic>) : this(channelsToRoles.toMap())

    fun getRole(channel: String, topic: String): String? =
        channelToRoleMap[channel]?.invoke(topic)
}