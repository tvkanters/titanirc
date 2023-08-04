package com.tvkdevelopment.titanirc.irc.listeners

import com.tvkdevelopment.titanirc.TitanircConfiguration
import com.tvkdevelopment.titanirc.irc.IrcMessageSender
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.Listener
import org.pircbotx.hooks.events.PrivateMessageEvent

class AdminListener(
    private val configuration: TitanircConfiguration,
    private val messageSender: IrcMessageSender,
) : Listener {

    override fun onEvent(event: Event?) {
        when (event) {
            is PrivateMessageEvent -> {
                if (event.user?.hostmask in configuration.ircAdminHostnames) {
                    return
                }

                event.message.extractCommand()?.apply {
                    when (command) {
                        "!topic",
                        "!settopic" ->
                            body?.split(" ", limit = 2)
                                ?.filter { it.isNotBlank() }
                                ?.let {
                                    messageSender.setTopic(it[0].trim(), it[1].trim())
                                }
                    }
                }
            }
        }
    }

    companion object {
        private fun String.extractCommand(): ExtractedCommand? {
            if (!startsWith('!')) return null

            val message = split(" ", limit = 2)
            val command = message[0]
            val body = message.takeIf { it.size > 1 }?.get(1)
            return ExtractedCommand(command, body)
        }

        private class ExtractedCommand(val command: String, val body: String?)
    }
}