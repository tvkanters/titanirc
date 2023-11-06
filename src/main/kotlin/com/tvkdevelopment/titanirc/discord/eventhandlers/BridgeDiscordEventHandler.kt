package com.tvkdevelopment.titanirc.discord.eventhandlers

import com.tvkdevelopment.titanirc.bridge.BridgeClient
import com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations.SnowflakeDecodeMessageTransformation
import com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations.escapeDiscordFormatting
import com.tvkdevelopment.titanirc.discord.*
import com.tvkdevelopment.titanirc.discord.eventhandlers.DiscordEventHandler.Registrar
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
import dev.kord.core.event.message.ReactionAddEvent
import kotlinx.coroutines.flow.firstOrNull

class BridgeDiscordEventHandler(
    private val listeners: List<BridgeClient.Listener>,
    snowflakeRegistry: SnowflakeRegistry
) : DiscordEventHandler {

    private val snowflakeDecodeMessageTransformation = SnowflakeDecodeMessageTransformation(snowflakeRegistry)
    private val lastMessages = mutableMapOf<Snowflake, MutableList<LastMessage>>()
    private val lastMessageReactions = mutableSetOf<LastMessageReaction>()

    private data class LastMessage(val member: Member, val messageId: Snowflake, val content: String)
    private data class LastMessageReaction(val member: Member, val emojiName: String)

    override fun Registrar.register() {
        on<MessageCreateEvent>({ guildId }) {
            val member = member ?: return@on
            val message = message.takeIf { it.type in RELAYED_MESSAGE_TYPES } ?: return@on

            guildId?.let { lastMessages.getOrPut(it) { mutableListOf() } }
                ?.addWithLimit(LastMessage(member, message.id, message.content), MESSAGE_CORRECTION_LIMIT)
            lastMessageReactions.clear()

            if (!member.isBot) {
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
        }

        onChannel<ChannelUpdateEvent>({ channel }) {
            val topic = channel.topicValue.trim()
            if (topic != old.topicValue.trim()) {
                Log.i("Discord topic updated: ${channel.data.name.value}")

                val channelString = channel.id.toString()
                if (old != null && topic.isNotBlank()) {
                    val topicRole = configuration.discordTopicRoles.getRole(channelString, topic)
                    val sanitizedTopic = topic.replace(TOPIC_UPDATE_SANITIZE_REGEX, "<$1>")
                    channel.asChannelOfOrNull<MessageChannel>()
                        ?.createMessage(":bell: ${topicRole?.mentionRole ?: "Topic"} updated: $sanitizedTopic")
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

            val channel = new.channelId.toString()
            fun String.takeForWordCorrection() =
                takeIf { it.isNotBlank() }
                    ?.let { snowflakeDecodeMessageTransformation.transform(channel, channel, it) }

            val oldContent = originalMessage.content.takeForWordCorrection() ?: return@on
            val newContent = new.content.value?.takeForWordCorrection() ?: return@on

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

        on<ReactionAddEvent>({ guildId }) {
            if (messageId == lastMessages[guildId]?.lastOrNull()?.messageId) {
                val member = getUserAsMember() ?: return@on

                val lastMessageReaction = LastMessageReaction(member, emoji.name)
                if (lastMessageReaction !in lastMessageReactions) {
                    lastMessageReactions += lastMessageReaction

                    val memberName = member.effectiveName
                    listeners.forEach {
                        it.onMessage(message.channelId.toString(), memberName, emoji.mention)
                    }
                }
            }
        }
    }

    companion object {
        private const val MESSAGE_CORRECTION_LIMIT = 10
        private val TOPIC_UPDATE_SANITIZE_REGEX = Regex("(?<!<)(https?:\\/\\/[^ ]+)")

        private val RELAYED_MESSAGE_TYPES = setOf(
            MessageType.Default,
            MessageType.Reply,
        )

        private val MessageUpdateEvent.guildId: Snowflake?
            get() = new.guildId.value
    }
}