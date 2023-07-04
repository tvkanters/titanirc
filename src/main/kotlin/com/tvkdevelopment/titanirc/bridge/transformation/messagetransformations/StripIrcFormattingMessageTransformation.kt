package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import org.pircbotx.Colors.*

class StripIrcFormattingMessageTransformation : MessageTransformation {
    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message.replace(FORMATTING_REGEX, "")

    companion object {
        private val FORMATTING_REGEX = Regex("""$NORMAL|$BOLD|$ITALICS|$UNDERLINE|$REVERSE|\u0003(?:\d\d?(?:,\d\d?)?)?""")
    }
}