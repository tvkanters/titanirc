package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation

class VxMessageTransformation : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message
            .replace(VX_REGEX, "https://vx$1.")
            .replace(REDD_IT_REGEX, "https://vxreddit.com/")

    companion object {
        private val VX_REGEX = Regex("""https?://(?:www\.|old\.)?(twitter|threads|tiktok|reddit)\.""")
        private val REDD_IT_REGEX = Regex("""https?://((?:old|www)\.)?(redd\.it)/""")
    }
}