package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import com.tvkdevelopment.titanirc.discord.SnowflakeRegistry

class SnowflakeEncodeEmojiMessageTransformation(
    private val snowflakeRegistry: SnowflakeRegistry,
) : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String {
        val emojiRegistry = snowflakeRegistry.forChannel(targetChannel)?.emojiRegistry ?: return message

        return message.replace(message.emojiRegex) { match ->
            match.groupValues[1]
                .lowercase()
                .let { emojiRegistry.itemsByNormalizedName[it] }
                ?.let { id ->
                    emojiRegistry.itemsById[id]
                        ?.let { "<:${it.originalName}:${id}>" }
                }
                ?: match.groupValues[0]
        }
    }

    companion object {
        private val REGEX_POTENTIAL_EMOJI_NAME =
            Regex(""":([a-z0-9_-]+):""", RegexOption.IGNORE_CASE)
        private val REGEX_POTENTIAL_EMOJI_NAME_SAFE =
            Regex("""(?<=^| ):([a-z0-9_-]+):""", RegexOption.IGNORE_CASE)

        private val String.emojiRegex: Regex
            get() = if (contains("://")) REGEX_POTENTIAL_EMOJI_NAME_SAFE else REGEX_POTENTIAL_EMOJI_NAME
    }
}
