package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import org.pircbotx.Colors

class ConvertIrcFormattingToDiscordMessageTransformation : MessageTransformation {
    override fun transform(message: String): String =
        FORMATTINGS.fold(message) { transformedMessage, formatting -> formatting.apply(transformedMessage) }

    companion object {
        private val FORMATTINGS = listOf(
            Formatting(Colors.BOLD, "**"),
            Formatting(Colors.ITALICS, "_"),
            Formatting("\u00031,1", "||"),
            // Underlines are disabled because they can conflict with italics
            //Formatting(Colors.UNDERLINE, "__"),
        )

        private class Formatting(private val ircSymbol: String, private val discordSymbol: String) {
            private val regex = Regex("""$ircSymbol([^${Colors.NORMAL}$ircSymbol]+)$ircSymbol?""")

            fun apply(message: String) =
                message.replace(regex) {
                    discordSymbol +
                        FORMATTINGS.fold(it.groupValues[1]) { group, formatting ->
                            group.replace(
                                formatting.ircSymbol,
                                "$discordSymbol${formatting.ircSymbol}$discordSymbol"
                            )
                        } +
                        discordSymbol
                }
        }
    }
}