package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation

class VxStripMessageTransformation : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message
            .replace(INSTAGRAMEZ_REGEX, "https://$1instagram.")

    companion object {
        private val INSTAGRAMEZ_REGEX = Regex("""https?://(www\.)?instagramez\.""")
    }
}