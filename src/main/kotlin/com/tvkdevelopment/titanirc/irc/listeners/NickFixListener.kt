package com.tvkdevelopment.titanirc.irc.listeners

import com.tvkdevelopment.titanirc.TitanircConfiguration
import org.pircbotx.PircBotX
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.Listener
import org.pircbotx.hooks.events.QuitEvent

class NickFixListener(private val configuration: TitanircConfiguration) : Listener {

    override fun onEvent(event: Event?) {
        when (event) {
            is QuitEvent -> {
                if (event.user?.nick == configuration.ircNick) {
                    event.getBot<PircBotX>().sendIRC().changeNick(configuration.ircNick)
                }
            }
        }
    }
}