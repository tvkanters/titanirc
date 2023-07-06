package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import com.tvkdevelopment.titanirc.discord.SnowflakeRegistry
import dev.kord.common.entity.Snowflake

class DecodeDiscordSnowflakeMessageTransformation(private val snowflakeRegistry: SnowflakeRegistry) :
    MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String {
        val snowflakeRegistry = snowflakeRegistry.forChannel(sourceChannel) ?: return message
        return message
            .replace(REGEX_EMOJI, "$1")
            .replace(REGEX) { match ->
                match.groupValues[2]
                    .toLongOrNull()
                    ?.let { id ->
                        val snowflake = Snowflake(id)
                        when (match.groupValues[1]) {
                            SYMBOL_USERNAME ->
                                snowflakeRegistry.memberRegistry.itemsById[snowflake]?.originalName

                            SYMBOL_CHANNEL ->
                                snowflakeRegistry.channelRegistry.itemsById[snowflake]?.originalName?.let { "#$it" }

                            else ->
                                null
                        }
                    }
                    ?: FALLBACK_NAME
            }
    }

    companion object {
        private const val SYMBOL_USERNAME = "@"
        private const val SYMBOL_CHANNEL = "#"
        private const val SYMBOL_ROLE = "@&"
        private const val FALLBACK_NAME = "???"

        private val REGEX = Regex("""<($SYMBOL_USERNAME|$SYMBOL_CHANNEL)(\d+)>""", RegexOption.IGNORE_CASE)

        private val REGEX_EMOJI = Regex("""<a?(:[a-z0-9_-]+:)\d+>""", RegexOption.IGNORE_CASE)
    }
}
