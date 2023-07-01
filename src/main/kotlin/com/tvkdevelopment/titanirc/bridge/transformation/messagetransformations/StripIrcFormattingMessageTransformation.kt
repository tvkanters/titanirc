package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import org.pircbotx.Colors

class StripIrcFormattingMessageTransformation : MessageTransformation {
    override fun transform(message: String): String =
        message.replace(FORMATTING_REGEX, "")

    companion object {
        private val FORMATTING_REGEX = Regex(Colors.LOOKUP_TABLE.values.joinToString("|"))
    }
}