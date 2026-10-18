package com.tvkdevelopment.titanirc.irc.listeners

import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IrcHealthListenerTest {

    @Test
    fun `a parsed PONG with the same token prevents the probe from expiring`() {
        val token = "titanirc-123456789"
        val pendingPong = AtomicReference<String?>(token)
        val parsedReply = String(token.toCharArray())

        assertFalse(token === parsedReply)
        pendingPong.acknowledgePong(parsedReply)

        assertNull(pendingPong.get())
        assertFalse(pendingPong.compareAndSet(token, null))
    }

    @Test
    fun `an unrelated PONG leaves the current probe outstanding`() {
        val token = "titanirc-123456789"
        val pendingPong = AtomicReference<String?>(token)

        pendingPong.acknowledgePong("titanirc-older-probe")

        assertEquals(token, pendingPong.get())
        assertTrue(pendingPong.compareAndSet(token, null))
    }
}
