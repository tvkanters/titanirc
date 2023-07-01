package com.tvkdevelopment.titanirc.irc.listeners

import com.tvkdevelopment.titanirc.util.Log
import org.pircbotx.PircBotX
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.Listener
import org.pircbotx.hooks.events.*

class LogListener : Listener {
    override fun onEvent(event: Event?) {
        when (event) {
            is SocketConnectEvent -> Log.i("Connecting: ${event.getBot<PircBotX>().serverHostname}")
            is JoinEvent -> {
                if (event.getBot<PircBotX>().nick == event.user?.nick) {
                    Log.i("Joined: ${event.channel.name}")
                }
            }
            is DisconnectEvent -> Log.i("Disconnected: ${event.disconnectException}")
            is ConnectAttemptFailedEvent -> Log.w("Connection failed: ${event.connectExceptions}")
            is ExceptionEvent -> Log.e(event.exception)
        }
    }
}