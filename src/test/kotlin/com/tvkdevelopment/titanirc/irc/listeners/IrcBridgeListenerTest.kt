package com.tvkdevelopment.titanirc.irc.listeners

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IrcBridgeListenerTest {

    @Test
    fun testUrlOnlyMessageFromConfiguredUsername() {
        val usernames = setOf("botident")

        assertTrue(shouldOmitIrcBotNick("SomeNick", "BotIdent", "https://example.com/a", usernames, emptySet()))
        assertTrue(shouldOmitIrcBotNick("ChangedNick", "botident", "http://example.com", usernames, emptySet()))
        assertTrue(shouldOmitIrcBotNick("ChangedNick", "botident", "HTTPS://example.com", usernames, emptySet()))
        assertFalse(shouldOmitIrcBotNick("SomeNick", "otherident", "https://example.com", usernames, emptySet()))
        assertFalse(shouldOmitIrcBotNick("SomeNick", null, "https://example.com", usernames, emptySet()))
    }

    @Test
    fun testUrlOnlyMessageFromConfiguredNick() {
        val nicks = setOf("LinkBot")

        assertTrue(shouldOmitIrcBotNick("linkbot", null, "https://example.com", emptySet(), nicks))
        assertFalse(shouldOmitIrcBotNick("OtherBot", null, "https://example.com", emptySet(), nicks))
    }

    @Test
    fun testNonUrlOnlyMessagesKeepNickname() {
        val usernames = setOf("botident")
        val nicks = setOf("LinkBot")

        for (message in listOf(
            "Check https://example.com",
            "https://example.com more",
            "https://example.com\nmore",
            "https://example.com ",
            "httpish",
            "!command",
        )) {
            assertFalse(shouldOmitIrcBotNick("LinkBot", "botident", message, usernames, nicks), message)
        }
    }

    @Test
    fun testNoConfiguredBotsKeepsNickname() {
        assertFalse(shouldOmitIrcBotNick("LinkBot", "botident", "https://example.com", emptySet(), emptySet()))
    }
}
