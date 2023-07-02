package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import org.pircbotx.Colors

class ConvertDiscordFormattingToIrcMessageTransformation : MessageTransformation {
    override fun transform(message: String): String =
        FORMATTINGS.fold(message) { transformedMessage, formatting -> formatting.apply(transformedMessage) }

    companion object {
        private val FORMATTINGS = listOf(
            Formatting("**", Colors.BOLD),
            Formatting("__", Colors.UNDERLINE),
            Formatting("_", Colors.ITALICS),
            Formatting("*", Colors.ITALICS),
            Formatting("||", "\u00031,1"),
        )

        private class Formatting(discordSymbol: String, private val ircSymbol: String) {
            private val regex = Regex.escape(discordSymbol).let { Regex("""$it([^$it]+)$it?""") }

            fun apply(message: String) =
                message.replace(regex, "$ircSymbol$1$ircSymbol")
        }
    }
}