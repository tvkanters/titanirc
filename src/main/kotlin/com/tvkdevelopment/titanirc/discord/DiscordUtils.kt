package com.tvkdevelopment.titanirc.discord

import com.tvkdevelopment.titanirc.util.Time
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.StickerItem
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.effectiveName

val Channel?.topicValue: String
    get() = this?.data?.topic?.value ?: ""

val Message.replyLabel: String
    get() {
        val authorName = author?.let { "${it.effectiveName} " }
        val timeString = Time.getRelativeTimeString(Time.currentTime - timestamp, short = true)
        return "[^$authorName$timeString]"
    }

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
