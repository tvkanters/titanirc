package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation

class DiscordUrlTrackingStripMessageTransformation : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message.replace(REGEX, "$1")

    companion object {
        private val REGEX = Regex("""(https?://(?:media|cdn)\.discordapp\.(?:net|com)/attachments/[^?]+)\?[^ ]+""")
    }
}