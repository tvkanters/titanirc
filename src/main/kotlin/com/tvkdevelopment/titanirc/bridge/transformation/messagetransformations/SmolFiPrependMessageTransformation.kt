package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import java.net.URLEncoder

class SmolFiPrependMessageTransformation : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message.replace(REGEX) { "https://smol.fi/e/?v=https://media.discordapp.net/attachments/${encode(it.groupValues[1])}" }

    companion object {
        private val REGEX =
            Regex("""(?<!https://smol.fi/e/\?v=)https?://(?:media|cdn)\.discordapp\.(?:net|com)/attachments/(\S+\.(?:mp4|webm|mov)[^ ]*)""")

        private fun encode(string: String) = URLEncoder.encode(string, Charsets.UTF_8.name())
    }
}