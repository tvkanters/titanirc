package com.tvkdevelopment.titanirc.irc.listeners

import org.pircbotx.PircBotX
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.Listener
import org.pircbotx.hooks.events.QuitEvent

class NickFixListener(private val desiredNick: String) : Listener {

    override fun onEvent(event: Event?) {
        when (event) {
            is QuitEvent -> {
                if (event.user?.nick == desiredNick) {
                    event.getBot<PircBotX>().sendIRC().changeNick(desiredNick)
                }
            }
        }
    }
}