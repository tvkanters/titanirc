package com.tvkdevelopment.titanirc.discord

import com.tvkdevelopment.titanirc.util.Time
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.StickerItem
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.effectiveName

val Channel?.topicValue: String
    get() = this?.data?.topic?.value ?: ""

suspend fun Message.getReplyLabel(): String =
    mutableListOf<String>()
        .asSequence()
        .plus(
            when {
                author?.isBot == true -> bridgeNickname
                else -> getAuthorAsMemberOrNull()?.effectiveName ?: author?.effectiveName
            }
        )
        .plus(Time.getRelativeTimeString(Time.currentTime - timestamp, short = true))
        .filterNot { it.isNullOrEmpty() }
        .joinToString(" ")
        .let { "[^ $it]" }

val Message.bridgeNickname: String?
    get() =
        BRIDGE_DISCORD_NICKNAME
            .matchAt(content, 0)
            ?.run {
                groupValues
                    .drop(1)
                    .firstOrNull { it.isNotBlank() }
            }

private val BRIDGE_DISCORD_NICKNAME = Regex("""^(?:\*\*<([^>]+)>\*\*|\\\* ([^ ]+)) """)

val StickerItem.label: String
    get() = "[$name sticker]"

const val MENTION_SYMBOL_MEMBER = "@"
const val MENTION_SYMBOL_ROLE = "@&"
const val MENTION_SYMBOL_CHANNEL = "#"

val Snowflake.mentionMember: String
    get() = "<$MENTION_SYMBOL_MEMBER$this>"

val Snowflake.mentionRole: String
    get() = "<$MENTION_SYMBOL_ROLE$this>"

val Snowflake.mentionChannel: String
    get() = "<$MENTION_SYMBOL_CHANNEL$this>"

private val TOPIC_UPDATE_SANITIZE_REGEX = Regex("""(?<!<)(https?://[^ )]+)""")

fun escapeTopicForMessage(topic: String) =
    topic.replace(TOPIC_UPDATE_SANITIZE_REGEX, "<$1>")

