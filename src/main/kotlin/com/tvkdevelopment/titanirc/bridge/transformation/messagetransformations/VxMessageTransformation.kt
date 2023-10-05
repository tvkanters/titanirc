package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation

class VxMessageTransformation : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message.replace(REGEX, "https://vx$1.")

    companion object {
        private val REGEX = Regex("""https?://(?:www\.)?(twitter|threads|tiktok)\.""")
    }
}