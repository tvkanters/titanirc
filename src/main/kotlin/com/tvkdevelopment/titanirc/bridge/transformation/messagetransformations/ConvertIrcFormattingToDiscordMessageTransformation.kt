package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import org.pircbotx.Colors

class ConvertIrcFormattingToDiscordMessageTransformation : MessageTransformation {
    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        FORMATTINGS.fold(message) { transformedMessage, formatting -> formatting.apply(transformedMessage) }

    companion object {
        private val FORMATTINGS = listOf(
            Formatting(Colors.BOLD, discordSymbol = "**"),
            Formatting(Colors.ITALICS, discordSymbol = "_"),
            Formatting("\u00031,1", listOf(Colors.NORMAL, "\u0003", "\u00031,1"), discordSymbol = "||"),
            // Underlines are disabled because they can conflict with italics
            //Formatting(Colors.UNDERLINE, "__"),
        )

        private class Formatting(
            private val ircSymbol: String,
            ircClosingSymbols: List<String> = listOf(Colors.NORMAL, ircSymbol),
            private val discordSymbol: String,
        ) {
            private val regex = Regex("""$ircSymbol((?:(?!${ircClosingSymbols.joinToString("|")}).)+)(?:$ircSymbol)?""")

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