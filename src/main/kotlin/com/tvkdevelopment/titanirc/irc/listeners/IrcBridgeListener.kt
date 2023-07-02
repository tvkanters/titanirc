package com.tvkdevelopment.titanirc.irc.listeners

import com.tvkdevelopment.titanirc.bridge.BridgeClient
import org.pircbotx.Channel
import org.pircbotx.User
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.Listener
import org.pircbotx.hooks.events.ActionEvent
import org.pircbotx.hooks.events.MessageEvent
import org.pircbotx.hooks.events.TopicEvent

class IrcBridgeListener(
    private val messageListeners: List<BridgeClient.MessageListener>,
    private val slashMeListeners: List<BridgeClient.SlashMeListener>,
    private val topicListeners: List<BridgeClient.TopicListener>,
) : Listener {

    override fun onEvent(event: Event?) {
        when (event) {
            is MessageEvent ->
                onMessage(event.user, event.channel, event.message)

            is ActionEvent ->
                onSlashMe(event.user, event.channel, event.message)

            is TopicEvent ->
                topicListeners.forEach { it.onTopicChanged(event.channel.name, event.topic) }
        }
    }

    private fun onMessage(user: User?, channel: Channel?, message: String) {
        val nick = user?.nick ?: return
        val channelName = channel?.name ?: return
        messageListeners.forEach { it.onMessage(channelName, nick, message) }
    }

    private fun onSlashMe(user: User?, channel: Channel?, message: String) {
        val nick = user?.nick ?: return
        val channelName = channel?.name ?: return
        slashMeListeners.forEach { it.onSlashMe(channelName, nick, message) }
    }
}