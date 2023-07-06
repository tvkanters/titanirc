package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation

class SmolFiPrependMessageTransformation : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message.replace(REGEX, "https://smol.fi/e/?v=https://media.discordapp.net/attachments/$1")

    companion object {
        private val REGEX =
            Regex("""(?<!https://smol.fi/e/\?v=)https?://(?:media|cdn)\.discordapp\.(?:net|com)/attachments/(\S+\.(?:mp4|webm|mov))""")
    }
}