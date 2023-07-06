package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import com.tvkdevelopment.titanirc.discord.SnowflakeRegistry

class HashtagToDiscordChannelMessageTransformation(
    private val snowflakeRegistry: SnowflakeRegistry,
) : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String {
        val channels = snowflakeRegistry.forChannel(targetChannel)?.channelRegistry?.itemsByNormalizedName
            ?: return message
        return message.replace(REGEX_POTENTIAL_MEMBER_NAME) { match ->
            match.groupValues[1]
                .lowercase()
                .let { channels[it] }
                ?.let { "<#$it>" }
                ?: match.groupValues[0]
        }
    }

    companion object {
        private val REGEX_POTENTIAL_MEMBER_NAME =
            Regex("""(?<=^| )#([a-z0-9_-]+)""", RegexOption.IGNORE_CASE)
    }
}
