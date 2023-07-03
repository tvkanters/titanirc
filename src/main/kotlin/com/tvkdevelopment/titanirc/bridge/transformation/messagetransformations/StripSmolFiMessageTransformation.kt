package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation

class StripSmolFiMessageTransformation : MessageTransformation {

    override fun transform(channel: String, message: String): String =
        message.replace(REGEX, "")

    companion object {
        private val REGEX = Regex("""https?://smol\.fi/e/\?v=""")
    }
}