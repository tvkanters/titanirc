package com.tvkdevelopment.titanirc.irc.listeners

import com.tvkdevelopment.titanirc.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.pircbotx.PircBotX
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.Listener
import org.pircbotx.hooks.events.ConnectEvent
import org.pircbotx.hooks.events.DisconnectEvent
import org.pircbotx.hooks.events.UnknownEvent
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.seconds

/** Checks that IRC is responding and that the bridge remains in its configured channels. */
class IrcHealthListener(
    private val scope: CoroutineScope,
    private val channels: Set<String>,
    private val restart: (PircBotX) -> Unit,
) : Listener {

    private var watchdog: Job? = null
    private val pendingPong = AtomicReference<String?>(null)

    override fun onEvent(event: Event?) {
        when (event) {
            is ConnectEvent -> {
                watchdog?.cancel()
                val bot = event.getBot<PircBotX>()
                watchdog = scope.launch {
                    // Registration happens before PircBotX sends its automatic JOIN commands.
                    delay(JOIN_GRACE_PERIOD)
                    while (isActive && bot.state == PircBotX.State.CONNECTED) {
                        channels.filterNot { bot.userChannelDao.containsChannel(it) }
                            .forEach { channel ->
                                Log.w("IRC is missing $channel; attempting to rejoin")
                                try {
                                    bot.sendIRC().joinChannel(channel)
                                } catch (e: Exception) {
                                    Log.e("Could not rejoin IRC channel $channel", e)
                                }
                            }

                        val token = "titanirc-${System.nanoTime()}"
                        pendingPong.set(token)
                        launch(Dispatchers.IO) {
                            try {
                                bot.sendRaw().rawLineNow("PING :$token")
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                if (pendingPong.compareAndSet(token, null)) {
                                    if (bot.state == PircBotX.State.CONNECTED) {
                                        Log.e("IRC health PING failed", e)
                                        restart(bot)
                                    }
                                }
                            }
                        }
                        delay(PONG_TIMEOUT)
                        if (pendingPong.compareAndSet(token, null)) {
                            if (bot.state == PircBotX.State.CONNECTED) {
                                Log.w("IRC did not respond to the health PING; reconnecting")
                                restart(bot)
                            }
                            return@launch
                        }
                        delay(PING_INTERVAL)
                    }
                }
            }

            is UnknownEvent -> {
                if (event.command.equals("PONG", ignoreCase = true)) {
                    pendingPong.compareAndSet(event.parsedLine.lastOrNull(), null)
                }
            }

            is DisconnectEvent -> {
                watchdog?.cancel()
                watchdog = null
                pendingPong.set(null)
            }
        }
    }

    companion object {
        private val JOIN_GRACE_PERIOD = 30.seconds
        private val PING_INTERVAL = 60.seconds
        private val PONG_TIMEOUT = 20.seconds
    }
}
