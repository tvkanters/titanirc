package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation

class EscapeDiscordFormattingMessageTransformation : MessageTransformation {
    override fun transform(message: String): String =
        message.replace(REGEX, "\\\\$1")

    companion object {
        private val REGEX = Regex("""([*_~])""")
    }
}