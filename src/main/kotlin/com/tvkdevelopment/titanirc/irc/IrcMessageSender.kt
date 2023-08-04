package com.tvkdevelopment.titanirc.irc

import com.tvkdevelopment.titanirc.TitanircConfiguration
import com.tvkdevelopment.titanirc.util.Log
import kotlinx.coroutines.*
import org.pircbotx.PircBotX
import org.pircbotx.ReplyConstants
import org.pircbotx.hooks.Event
import org.pircbotx.hooks.Listener
import org.pircbotx.hooks.events.ServerResponseEvent
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates.observable
import kotlin.time.Duration.Companion.minutes

/**
 * Offers sending messages to IRC with flood protection.
 */
class IrcMessageSender(
    private val scope: CoroutineScope,
    private val configuration: TitanircConfiguration,
    private val getBot: () -> PircBotX?
) : Listener {

    private var virtualBufferTimeout: Job? = null

    private var virtualBufferClearCountdown: CountDownLatch? = null
    private var virtualBuffer by observable(0) { _, _, newValue ->
        log("Virtual buffer size: $newValue")
        virtualBufferTimeout?.cancel()
        if (newValue != 0) {
            clearVirtualBufferAsync()
        }
    }

    private fun clearVirtualBufferAsync() {
        virtualBufferTimeout = scope.async {
            delay(CLEAR_VIRTUAL_BUFFER_TIMEOUT)
            virtualBufferTimeout = null
            virtualBuffer = 0
            log("Cleared virtual buffer async")
        }
    }

    fun sendMessage(channel: String, message: String) {
        onBot {
            sendRawLine("PRIVMSG $channel :$message")
        }
    }

    fun setTopic(channel: String, topic: String) {
        onBot {
            userChannelDao.getChannel(channel)
                .takeIf { it.topic != topic }
                ?.apply { sendRawLine("PRIVMSG Q :SETTOPIC $channel $topic") }
        }
    }

    private fun onBot(block: PircBotX.() -> Unit) {
        scope.launch {
            try {
                getBot()
                    ?.takeIf { it.state == PircBotX.State.CONNECTED }
                    ?.block()
            } catch (e: Exception) {
                Log.e(e)
            }
        }
    }

    private fun PircBotX.sendRawLine(message: String) {
        val messageSize = message.toByteArray().size
        if (virtualBuffer + messageSize > VIRTUAL_BUFFER_CAPACITY) {
            log("Clearing virtual buffer before sending: $message")
            clearVirtualBufferSync()
        }

        log("Sending: $message")
        virtualBuffer += messageSize
        sendRaw().rawLineNow(message)
    }

    private fun PircBotX.clearVirtualBufferSync() {
        val countDownLatch = CountDownLatch(1)
        virtualBufferClearCountdown = countDownLatch

        sendRaw().rawLineNow(COMMAND_ANTIFLOOD)
        countDownLatch.await(CLEAR_VIRTUAL_BUFFER_TIMEOUT.inWholeMilliseconds, TimeUnit.MILLISECONDS)

        virtualBufferClearCountdown = null
        virtualBuffer = 0
        log("Cleared virtual buffer sync")
    }

    override fun onEvent(event: Event?) {
        when (event) {
            is ServerResponseEvent -> {
                if (event.code == ReplyConstants.ERR_UNKNOWNCOMMAND && event.parsedResponse[1] == COMMAND_ANTIFLOOD) {
                    virtualBufferClearCountdown?.countDown()
                }
            }
        }
    }

    private fun log(message: String) {
        if (configuration.isDevEnv) {
            Log.i("IrcMessageSender: $message")
        }
    }

    companion object {
        private const val COMMAND_ANTIFLOOD = "ANTIFLOOD"
        /** The amount of bytes Quakenet can accept before potentially triggering flood protection. */
        private const val VIRTUAL_BUFFER_CAPACITY = 1024 - COMMAND_ANTIFLOOD.length

        private val CLEAR_VIRTUAL_BUFFER_TIMEOUT = 1.minutes
    }
}