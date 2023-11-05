package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation

class VxMessageTransformation : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message
            .replace(VX_REGEX, "https://vx$1.")
            .replace(REDDIT_REGEX, "https://$1rxddit.com/")

    companion object {
        private val VX_REGEX = Regex("""https?://(?:www\.)?(twitter|threads|tiktok)\.""")
        private val REDDIT_REGEX = Regex("""https?://((?:old|www)\.)?(reddit\.[^/.]+|redd\.it)/""")
    }
}