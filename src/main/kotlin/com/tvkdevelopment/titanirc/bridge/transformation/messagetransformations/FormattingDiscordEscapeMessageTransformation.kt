package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation

class FormattingDiscordEscapeMessageTransformation : MessageTransformation {
    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        buildString {
            var illegalStringPosition = 0
            var withinIllegalString = false
            message.forEach { char ->
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

    companion object {
        private val ESCAPED_CHARS = setOf('*', '_', '~', '`', '\\')
        private const val ILLEGAL_WORD = "://"
        private const val WORD_SEPARATOR = ' '
    }
}