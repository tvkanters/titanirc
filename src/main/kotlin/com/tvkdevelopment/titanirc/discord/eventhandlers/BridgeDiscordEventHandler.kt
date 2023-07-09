package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.bridge.BridgeClient
import com.tvkdevelopment.titanirc.discord.eventhandlers.DiscordEventHandler.Registrar
import com.tvkdevelopment.titanirc.discord.getReplyLabel
import com.tvkdevelopment.titanirc.discord.label
import com.tvkdevelopment.titanirc.discord.mentionRole
import com.tvkdevelopment.titanirc.discord.topicValue
import com.tvkdevelopment.titanirc.util.Log
import dev.kord.common.entity.MessageType
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.effectiveName
import dev.kord.core.event.channel.ChannelUpdateEvent
import dev.kord.core.event.channel.thread.TextChannelThreadCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.flow.firstOrNull

class BridgeDiscordEventHandler(private val listeners: List<BridgeClient.Listener>) : DiscordEventHandler {

    override fun Registrar.register() {
        on<MessageCreateEvent>({ guildId }) {
            val member = member?.takeUnless { it.isBot } ?: return@on
            val message = message.takeIf { it.type in RELAYED_MESSAGE_TYPES } ?: return@on
            val messageToSend = with(message) {
                mutableListOf<String>()
                    .asSequence()
                    .plus(referencedMessage?.getReplyLabel())
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

        onChannel<ChannelUpdateEvent>({ channel }) {
            val topic = channel.topicValue
            if (topic != old.topicValue) {
                Log.i("Discord topic updated: ${channel.data.name.value}")

                val channelString = channel.id.toString()
                if (old != null && topic.isNotBlank()) {
                    val topicRole = configuration.discordTopicRoles.getRole(channelString, topic)
                    channel.asChannelOfOrNull<MessageChannel>()
                        ?.createMessage(":bell: ${topicRole?.mentionRole ?: "Topic"} updated: $topic")
                }

                listeners.forEach {
                    it.onTopicChanged(channelString, topic)
                }
            }
        }

        onChannel<TextChannelThreadCreateEvent>({ channel }) {
            val lastMessage = channel.messages.firstOrNull { it.data.referencedMessage.value != null }
            val referencedMessage = lastMessage?.referencedMessage ?: return@onChannel

            val channelString = referencedMessage.channel.id.toString()
            val ownerName = channel.owner.asUserOrNull()?.effectiveName ?: return@onChannel
            val message = "created a thread ${referencedMessage.getReplyLabel()}"

            listeners.forEach {
                it.onSlashMe(channelString, ownerName, message)
            }
        }
    }

    companion object {
        private val RELAYED_MESSAGE_TYPES = setOf(
            MessageType.Default,
            MessageType.Reply,
        )
    }
}