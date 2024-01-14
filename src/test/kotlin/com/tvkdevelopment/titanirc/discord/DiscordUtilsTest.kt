package com.tvkdevelopment.titanirc.discord

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DiscordUtilsTest {

    @Test
    fun testEscapeTopicForMessage() {
        // GIVEN
        assertEquals("Test <http://test.com> Test", escapeTopicForMessage("Test http://test.com Test"))
    }

    @Test
    fun testEscapeTopicForMessageHyperlink() {
        // GIVEN
        assertEquals("[Test](<http://test.com>)", escapeTopicForMessage("[Test](http://test.com)"))
    }

    @Test
    fun testEscapeTopicForMessageNoDuplicate() {
        // GIVEN
        assertEquals("<http://test.com>", escapeTopicForMessage("<http://test.com>"))
    }

    @Test
    fun testEscapeTopicForMessageHyperlinkNoDuplicate() {
        // GIVEN
        assertEquals("[Test](<http://test.com>)", escapeTopicForMessage("[Test](<http://test.com>)"))
    }
}