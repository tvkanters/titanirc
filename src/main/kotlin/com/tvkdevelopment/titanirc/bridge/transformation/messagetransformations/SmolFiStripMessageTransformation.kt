package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation

class SmolFiStripMessageTransformation : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message.replace(REGEX, "https://")

    companion object {
        private val REGEX = Regex("""(?:https?://)?smol\.fi/e/\?v=(?:https?://)?""")
    }
}