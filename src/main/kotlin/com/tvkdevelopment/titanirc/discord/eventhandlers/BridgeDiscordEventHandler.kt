package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.bridge.BridgeClient
import com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations.escapeDiscordFormatting
import com.tvkdevelopment.titanirc.discord.eventhandlers.DiscordEventHandler.Registrar
import com.tvkdevelopment.titanirc.discord.getReplyLabel
import com.tvkdevelopment.titanirc.discord.label
import com.tvkdevelopment.titanirc.discord.mentionRole
import com.tvkdevelopment.titanirc.discord.topicValue
import com.tvkdevelopment.titanirc.util.Log
import com.tvkdevelopment.titanirc.util.addWithLimit
import com.tvkdevelopment.titanirc.util.calculateWordCorrection
import dev.kord.common.entity.MessageType
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.effectiveName
import dev.kord.core.event.channel.ChannelUpdateEvent
import dev.kord.core.event.channel.thread.TextChannelThreadCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageUpdateEvent
import kotlinx.coroutines.flow.firstOrNull

class BridgeDiscordEventHandler(private val listeners: List<BridgeClient.Listener>) : DiscordEventHandler {

    private val lastMessages = mutableMapOf<Snowflake, MutableList<LastMessage>>()

    private data class LastMessage(val member: Member, val messageId: Snowflake, val content: String)

    override fun Registrar.register() {
        on<MessageCreateEvent>({ guildId }) {
            val member = member?.takeUnless { it.isBot } ?: return@on
            val message = message.takeIf { it.type in RELAYED_MESSAGE_TYPES } ?: return@on

            guildId?.let { lastMessages.getOrPut(it) { mutableListOf() } }
                ?.addWithLimit(LastMessage(member, message.id, message.content), MESSAGE_CORRECTION_LIMIT)

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

        on<MessageUpdateEvent>({ guildId }) {
            val guildId = guildId ?: return@on
            val authorId = new.author.value?.id ?: return@on

            val lastGuildMessages = lastMessages[guildId] ?: return@on
            val lastMessageByAuthorIndex = lastGuildMessages.indexOfLast { it.member.id == authorId }
            val originalMessage =
                lastGuildMessages.getOrNull(lastMessageByAuthorIndex)?.takeIf { it.messageId == new.id } ?: return@on

            val oldContent = originalMessage.content.takeIf { it.isNotBlank() } ?: return@on
            val newContent = new.content.value?.takeIf { it.isNotBlank() } ?: return@on

            lastGuildMessages.removeAt(lastMessageByAuthorIndex)
            lastGuildMessages.add(lastMessageByAuthorIndex, originalMessage.copy(content = newContent))

            calculateWordCorrection(oldContent, newContent)
                ?.escapeDiscordFormatting()
                ?.let { correction ->
                    val authorName = originalMessage.member.effectiveName
                    listeners.forEach {
                        it.onMessage(message.channel.id.toString(), authorName, correction)
                    }
                }
        }
    }

    companion object {
        private const val MESSAGE_CORRECTION_LIMIT = 10

        private val RELAYED_MESSAGE_TYPES = setOf(
            MessageType.Default,
            MessageType.Reply,
        )

        private val MessageUpdateEvent.guildId: Snowflake?
            get() = new.guildId.value
    }
}