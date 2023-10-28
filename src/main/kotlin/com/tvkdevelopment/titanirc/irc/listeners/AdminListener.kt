package com.tvkdevelopment.titanirc.irc.listeners

import com.tvkdevelopment.titanirc.TitanircConfiguration
import com.tvkdevelopment.titanirc.irc.IrcMessageSender
import com.tvkdevelopment.titanirc.util.StreamInfo
import com.tvkdevelopment.titanirc.util.TopicUtil
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.Listener
import org.pircbotx.hooks.events.PrivateMessageEvent
import org.pircbotx.hooks.events.TopicEvent

class AdminListener(
    private val configuration: TitanircConfiguration,
    private val messageSender: IrcMessageSender,
) : Listener {

    private val channelToTopic = mutableMapOf<String, String>()

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

                        "!setstream" ->
                            body?.split(" ", limit = 3)
                                ?.filter { it.isNotBlank() }
                                ?.let { arguments ->
                                    val channel = arguments[0].trim()
                                    val streamer = arguments.getOrNull(1)?.trim() ?: ""
                                    val title = arguments.getOrNull(2)?.trim() ?: ""
                                    channelToTopic[channel]
                                        ?.let { TopicUtil.setStreamInfo(it, StreamInfo(streamer, "Game", title)) }
                                        ?.let { messageSender.setTopic(channel, it) }
                                }
                    }
                }
            }

            is TopicEvent ->
                channelToTopic[event.channel.name] = event.topic
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