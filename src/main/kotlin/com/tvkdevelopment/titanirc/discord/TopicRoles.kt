package com.tvkdevelopment.titanirc.discord

class TopicRoles(private val channelToRoleMap: Map<String, String>) {
    constructor(vararg channelsToRoles: Pair<String, String>) : this(channelsToRoles.toMap())

    fun getRole(channel: String): String? =
        channelToRoleMap[channel]
}