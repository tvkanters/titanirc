package com.tvkdevelopment.titanirc.irc.listeners

import com.tvkdevelopment.titanirc.bridge.BridgeClient
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.Listener
import org.pircbotx.hooks.events.MessageEvent

class IrcBridgeListener(private val listeners: List<BridgeClient.MessageListener>) : Listener {

    override fun onEvent(event: Event?) {
        when (event) {
            is MessageEvent -> {
                val nick = event.user?.nick ?: return
                listeners.forEach { it.onMessage(event.channel.name, nick, event.message) }
            }
        }
    }
}