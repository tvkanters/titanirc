package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import com.tvkdevelopment.titanirc.discord.MENTION_SYMBOL_CHANNEL
import com.tvkdevelopment.titanirc.discord.MENTION_SYMBOL_MEMBER
import com.tvkdevelopment.titanirc.discord.MENTION_SYMBOL_ROLE
import com.tvkdevelopment.titanirc.discord.SnowflakeRegistry
import com.tvkdevelopment.titanirc.util.ifNull
import dev.kord.common.entity.Snowflake

class SnowflakeDecodeMessageTransformation(private val snowflakeRegistry: SnowflakeRegistry) :
    MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String {
        val snowflakeRegistry = snowflakeRegistry.forChannel(sourceChannel) ?: return message
        return message
            .replace(REGEX_EMOJI) { match ->
                match.groupValues[1].let { EMOJI_REPLACEMENT[it] ?: it }
            }
            .replace(REGEX_SNOWFLAKE) { match ->
                match.groupValues[2]
                    .toLongOrNull()
                    ?.let { id ->
                        val snowflake = Snowflake(id)
                        when (match.groupValues[1]) {
                            MENTION_SYMBOL_MEMBER ->
                                snowflakeRegistry.memberRegistry.itemsById[snowflake]?.originalName

                            MENTION_SYMBOL_ROLE ->
                                snowflakeRegistry.roleRegistry.itemsById[snowflake]?.originalName?.let { "@$it" }

                            MENTION_SYMBOL_CHANNEL ->
                                snowflakeRegistry.channelRegistry.itemsById[snowflake]?.originalName?.let { "#$it" }
                                    ?.let { EMOJI_REPLACEMENT[it] ?: it }

                            else ->
                                null
                        }
                    }
                    .ifNull { FALLBACK_NAME }
                    .plus(match.groupValues[3])
            }
    }

    companion object {
        private const val FALLBACK_NAME = "???"

        private val REGEX_SNOWFLAKE =
            Regex(
                """<($MENTION_SYMBOL_MEMBER|$MENTION_SYMBOL_ROLE|$MENTION_SYMBOL_CHANNEL)(\d+)>(?: ([,.!?:]))?""",
                RegexOption.IGNORE_CASE
            )

        private val REGEX_EMOJI = Regex("""<a?(:[a-z0-9_-]+:)\d+>""", RegexOption.IGNORE_CASE)

        private val EMOJI_REPLACEMENT = mapOf(
            ":upset:" to ">:^|",
            ":pensive_cowboy:" to "\uD83D\uDE14",
            ":this:" to "^",
        )
    }
}
