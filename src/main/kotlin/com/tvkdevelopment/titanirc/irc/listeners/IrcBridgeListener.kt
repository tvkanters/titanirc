package com.tvkdevelopment.titanirc.irc.listeners

import com.tvkdevelopment.titanirc.bridge.BridgeClient
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.Listener
import org.pircbotx.hooks.events.MessageEvent
import org.pircbotx.hooks.events.TopicEvent

class IrcBridgeListener(
    private val messageListeners: List<BridgeClient.MessageListener>,
    private val topicListeners: List<BridgeClient.TopicListener>,
) : Listener {

    override fun onEvent(event: Event?) {
        when (event) {
            is MessageEvent -> {
                val nick = event.user?.nick ?: return
                messageListeners.forEach { it.onMessage(event.channel.name, nick, event.message) }
            }

            is TopicEvent -> {
                topicListeners.forEach { it.onTopicChanged(event.channel.name, event.topic) }
            }
        }
    }
}