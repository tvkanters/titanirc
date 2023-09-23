package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import org.pircbotx.Colors

class FormattingIrcToDiscordMessageTransformation : MessageTransformation {
    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message
            .takeUnless { it.contains("://") }
            ?.let { FORMATTINGS.fold(it) { transformedMessage, formatting -> formatting.apply(transformedMessage) } }
            ?: message

    companion object {
        private val FORMATTINGS = listOf(
            Formatting(Colors.BOLD, discordSymbol = "**"),
            // The shy character allows it working within words
            Formatting(Colors.ITALICS, discordSymbol = "_­"),
            Formatting("\u00031,1", listOf(Colors.NORMAL, "\u0003", "\u00031,1"), discordSymbol = "||"),
            // Underlines are disabled because they can conflict with italics
            //Formatting(Colors.UNDERLINE, "__"),
        )

        private class Formatting(
            private val ircSymbol: String,
            ircClosingSymbols: List<String> = listOf(Colors.NORMAL, ircSymbol),
            private val discordSymbol: String,
        ) {
            private val regex = ircClosingSymbols.joinToString("|").let { closingSymbols ->
                Regex("""$ircSymbol((?:(?!$closingSymbols).)+)(?:$ircSymbol)?""")
            }

            fun apply(message: String) =
                message.replace(regex) { match ->
                    val body = match.groupValues[1].trimEnd(' ')
                    val amountOfTrailingSpaces = match.groupValues[1].length - body.length

                    buildString {
                        append(discordSymbol)
                        append(FORMATTINGS.fold(body) { group, formatting ->
                            Regex(formatting.ircSymbol).findAll(group)
                                .toList()
                                .takeIf { it.size % 2 == 1 }
                                ?.last()
                                ?.range
                                ?.takeIf { it.first != 0 && it.last != group.length - 1 }
                                ?.let { group.replaceRange(it, "$discordSymbol${formatting.ircSymbol}$discordSymbol") }
                                ?: group
                        })
                        append(discordSymbol)
                        for (i in 0 until amountOfTrailingSpaces) {
                            append(' ')
                        }
                    }
                }
        }
    }
}