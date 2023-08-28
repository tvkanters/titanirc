package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import org.pircbotx.Colors

class FormattingDiscordToIrcMessageTransformation : MessageTransformation {
    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message
            .takeUnless { it.contains("://") || it.contains('`') }
            ?.let { FORMATTINGS.fold(it) { transformedMessage, formatting -> formatting.apply(transformedMessage) } }
            ?.unescapeDiscordFormatting()
            ?: message

    companion object {

        private val FORMATTINGS = listOf(
            Formatting("**", Colors.BOLD),
            Formatting("__", Colors.UNDERLINE),
            Formatting("_", Colors.ITALICS),
            Formatting("*", Colors.ITALICS),
            Formatting("||", "\u00031,1", "\u0003"),
        )

        private class Formatting(
            discordSymbol: String,
            private val ircSymbol: String,
            private val ircClosingSymbol: String = ircSymbol,
        ) {
            private val regex = Regex.escape(discordSymbol).let {
                Regex("""((?<!\\)(?:\\\\)*)$it((?:(?!$it).)+(?<!\\)(?:\\\\)*)$it""")
            }

            fun apply(message: String) =
                message.replace(regex, "$1$ircSymbol$2$ircClosingSymbol")
        }
    }
}