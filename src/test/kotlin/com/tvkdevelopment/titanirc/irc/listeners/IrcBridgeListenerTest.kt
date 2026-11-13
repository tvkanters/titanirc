package com.tvkdevelopment.titanirc.irc.listeners

import com.tvkdevelopment.titanirc.bridge.BridgeClient
import com.tvkdevelopment.titanirc.niceMockk
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.pircbotx.Channel
import org.pircbotx.User
import org.pircbotx.hooks.events.ActionEvent
import org.pircbotx.hooks.events.MessageEvent

class IrcBridgeListenerTest {

    private val bridgeListener = niceMockk<BridgeClient.Listener>()
    private val listener = IrcBridgeListener(listOf(bridgeListener))

    @Test
    fun testMessageIncludesIrcUsername() {
        val ircUser = niceMockk<User> {
            every { nick } returns "ChangedNick"
            every { login } returns "botident"
        }
        val ircChannel = niceMockk<Channel> {
            every { name } returns "#test"
        }
        val event = niceMockk<MessageEvent> {
            every { user } returns ircUser
            every { channel } returns ircChannel
            every { message } returns "https://example.com"
        }

        listener.onEvent(event)

        verify(exactly = 1) { bridgeListener.onMessage("#test", "botident", "ChangedNick", "https://example.com") }
    }

    @Test
    fun testActionIncludesIrcUsername() {
        val ircUser = niceMockk<User> {
            every { nick } returns "ChangedNick"
            every { login } returns "botident"
        }
        val ircChannel = niceMockk<Channel> {
            every { name } returns "#test"
        }
        val event = niceMockk<ActionEvent> {
            every { user } returns ircUser
            every { channel } returns ircChannel
            every { message } returns "waves"
        }

        listener.onEvent(event)

        verify(exactly = 1) { bridgeListener.onSlashMe("#test", "botident", "ChangedNick", "waves") }
    }

    @Test
    fun testMessageWithoutUserIsIgnored() {
        val event = niceMockk<MessageEvent> {
            every { user } returns null
        }

        listener.onEvent(event)

        verify(exactly = 0) { bridgeListener.onMessage(any(), any(), any(), any()) }
    }
}
