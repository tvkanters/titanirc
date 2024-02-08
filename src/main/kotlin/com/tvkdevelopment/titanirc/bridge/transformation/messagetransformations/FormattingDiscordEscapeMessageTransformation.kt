package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation

private val ESCAPED_CHARS = setOf('*', '_', '~', '`', '\\')
private const val ILLEGAL_WORD = "://"
private const val WORD_SEPARATOR = ' '

class FormattingDiscordEscapeMessageTransformation : MessageTransformation {
    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message.escapeDiscordFormatting()
}

fun String.escapeDiscordFormatting(): String =
    buildString {
        var illegalStringPosition = 0
        var withinIllegalString = false
        this@escapeDiscordFormatting.forEach { char ->
            if (!withinIllegalString) {
                if (char == ILLEGAL_WORD[illegalStringPosition]) {
                    ++illegalStringPosition
                    if (illegalStringPosition == ILLEGAL_WORD.length) {
                        illegalStringPosition = 0
                        withinIllegalString = true
                    }
                }
                if (char in ESCAPED_CHARS) {
                    append('\\')
                }
            } else {
                if (char == WORD_SEPARATOR) {
                    withinIllegalString = false
                }
            }
            append(char)
        }
    }

private val REGEX_UNESCAPE = Regex("""\\([*_|~#\\-])""")

fun String.unescapeDiscordFormatting() =
    replace(REGEX_UNESCAPE, "$1")