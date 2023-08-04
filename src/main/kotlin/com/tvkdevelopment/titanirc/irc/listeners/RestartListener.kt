package com.tvkdevelopment.titanirc.irc.listeners

import com.tvkdevelopment.titanirc.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.Listener
import org.pircbotx.hooks.events.DisconnectEvent
import org.pircbotx.hooks.events.SocketConnectEvent
import kotlin.time.Duration

class RestartListener(
    private val scope: CoroutineScope,
    private val restartDelay: Duration,
    private val restartFunction: () -> Unit
) : Listener {

    private var restartJob: Job? = null

    override fun onEvent(event: Event?) {
        when (event) {
            is SocketConnectEvent -> {
                restartJob?.apply {
                    Log.i("Reconnected - cancelling restart job")
                    cancel()
                    restartJob = null
                }
            }

            is DisconnectEvent -> {
                restartJob?.cancel()
                restartJob = scope.launch {
                    delay(restartDelay)
                    Log.i("IRC restarting")
                    restartFunction()
                }
            }
        }
    }
}