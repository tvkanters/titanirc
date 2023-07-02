package com.tvkdevelopment.titanirc.irc.listeners

import com.tvkdevelopment.titanirc.util.Log
import kotlinx.coroutines.*
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.Listener
import org.pircbotx.hooks.events.DisconnectEvent
import org.pircbotx.hooks.events.SocketConnectEvent

class RestartListener(private val restartDelayMs: Long, private val restartFunction: () -> Unit) : Listener {

    private var restartJob: Job? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onEvent(event: Event?) {
        when (event) {
            is SocketConnectEvent -> {
                restartJob?.cancel()
                restartJob = null
            }
            is DisconnectEvent -> {
                restartJob?.cancel()
                restartJob = GlobalScope.launch {
                    delay(restartDelayMs)
                    Log.i("IRC restarting")
                    restartFunction()
                }
            }
        }
    }
}