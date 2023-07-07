package com.tvkdevelopment.titanirc

import com.tvkdevelopment.titanirc.discord.TopicRoles
import com.tvkdevelopment.titanirc.util.TopicUtil

interface TitanircConfiguration {
    val isDevEnv: Boolean

    val ircUsername: String
    val ircPassword: String?
    val ircNick: String
    val ircChannels: Set<String>

    val discordToken: String
    val discordNick: String
        get() = "\uD83D\uDCAC"
    val topicRoles: TopicRoles
        get() = DEFAULT_TOPIC_ROLES
}

private val DEFAULT_TOPIC_ROLES = TopicRoles(
    "418911279625797652" to { topic -> 806471250406670367UL.takeIf { TopicUtil.getStreamInfo(topic) != null } },
)