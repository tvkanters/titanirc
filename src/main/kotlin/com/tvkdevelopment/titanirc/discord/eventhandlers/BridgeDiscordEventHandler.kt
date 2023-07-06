package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.TitanircConfiguration
import com.tvkdevelopment.titanirc.bridge.BridgeClient
import com.tvkdevelopment.titanirc.discord.label
import com.tvkdevelopment.titanirc.discord.mentionRole
import com.tvkdevelopment.titanirc.discord.replyLabel
import com.tvkdevelopment.titanirc.discord.topicValue
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.channel.ChannelUpdateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on

class BridgeDiscordEventHandler(
    private val configuration: TitanircConfiguration,
    private val listeners: List<BridgeClient.Listener>,
) : DiscordEventHandler {

    override fun Kord.register() {
        on<MessageCreateEvent> {
            member
                ?.takeUnless { it.isBot }
                ?.let { member ->
                    val messageToSend = with(message) {
                        mutableListOf<String>()
                            .asSequence()
                            .plus(referencedMessage?.replyLabel)
                            .plus(content)
                            .plus(stickers.map { it.label })
                            .plus(attachments.map { it.url }.filter { it !in content })
                            .filterNot { it.isNullOrBlank() }
                            .joinToString(" ")
                    }
                    listeners.forEach {
                        it.onMessage(message.channel.id.toString(), member.effectiveName, messageToSend)
                    }
                }
        }

        on<ChannelUpdateEvent> {
            val topic = channel.topicValue
            if (topic != old.topicValue) {
                Log.i("Discord topic updated: ${channel.data.name.value}")

                val channelString = channel.id.toString()
                if (old != null && topic.isNotBlank()) {
                    val topicRole = configuration.topicRoles.getRole(channelString, topic)
                    channel.asChannelOfOrNull<MessageChannel>()
                        ?.createMessage(":bell: ${topicRole?.mentionRole ?: "Topic"} updated: $topic")
                }

                listeners.forEach { it.onTopicChanged(channelString, topic) }
            }
        }
    }
}