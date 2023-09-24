package com.tvkdevelopment.titanirc

import com.tvkdevelopment.titanirc.discord.TopicRoles
import com.tvkdevelopment.titanirc.util.TopicUtil

interface TitanircConfiguration {
    val isDevEnv: Boolean

    val ircUsername: String
    val ircPassword: String?
    val ircNick: String
    val ircChannels: Set<String>
    val ircAdminHostnames: Set<String> get() = emptySet()

    val discordToken: String
    val discordNick: String
        get() = "\uD83D\uDCAC"
    val discordGuilds: Set<String>
        get() = DEFAULT_GUILDS
    val discordTopicRoles: TopicRoles
        get() = DEFAULT_TOPIC_ROLES
    val discordThreadsToPreserve: Set<String> get() = emptySet()
    val discordChannelTopicsToPreserve: Set<String> get() = emptySet()
    val discordEventSyncGuildToChannel: Map<String, String>
        get() = DEFAULT_EVENT_SYNC
}

const val DISCORD_ID_GUILD_DOPELIVES = "119177492253769743"
const val DISCORD_ID_CHANNEL_DOPELIVES_STREAMIRC = "418911279625797652"

val DEFAULT_GUILDS = setOf(DISCORD_ID_GUILD_DOPELIVES)
val DEFAULT_TOPIC_ROLES = TopicRoles(
    DISCORD_ID_CHANNEL_DOPELIVES_STREAMIRC to { topic -> 806471250406670367UL.takeIf { TopicUtil.getStreamInfo(topic) != null } },
)
val DEFAULT_EVENT_SYNC = mapOf(
    DISCORD_ID_GUILD_DOPELIVES to DISCORD_ID_CHANNEL_DOPELIVES_STREAMIRC,
)