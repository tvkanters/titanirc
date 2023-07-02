package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation

class AddTwitterFixMessageTransformation : MessageTransformation {

    override fun transform(message: String): String =
        message.replace(REGEX, "https://vxtwitter.")

    companion object {
        private val REGEX = Regex("""https?://(?:www\.)?twitter\.""")
    }
}