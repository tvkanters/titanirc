package com.tvkdevelopment.titanirc.irc

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MessageSplitterTest {

    @Test
    fun testSplitMessage() {
        // GIVEN
        val message = "This is a too long message"

        // WHEN
        val splitMessage = message.splitMessageForIrc(10)

        // THEN
        assertEquals(listOf("This is a", "too long", "message"), splitMessage)
    }

    @Test
    fun testSplitMessageWithPrefix() {
        // GIVEN
        val message = "This is a too long message"

        // WHEN
        val splitMessage = message.splitMessageForIrc(12, prefix = "<a> ")

        // THEN
        assertEquals(listOf("<a> This is", "<a> a too", "<a> long", "<a> message"), splitMessage)
    }

    @Test
    fun testSplitMessageWithPrefixAndNewlines() {
        // GIVEN
        val message = "This is a too long message\n\nwith new lines"

        // WHEN
        val splitMessage = message.splitMessageForIrc(12, prefix = "<a> ")

        // THEN
        assertEquals(listOf("<a> This is", "<a> a too", "<a> long", "<a> message", "<a> with new", "<a> lines"), splitMessage)
    }

    @Test
    fun testSplitLongWord() {
        // GIVEN
        val message = "https://www.way.too/long"

        // WHEN
        val splitMessage = message.splitMessageForIrc(15)

        // THEN
        assertEquals(listOf("https://www.…"), splitMessage)
    }

    @Test
    fun testSplitLongWordWithPrefix() {
        // GIVEN
        val message = "https://www.way.too/long"

        // WHEN
        val splitMessage = message.splitMessageForIrc(15, prefix = "<a> ")

        // THEN
        assertEquals(listOf("<a> https://…"), splitMessage)
    }

    @Test
    fun testExtendedAscii() {
        // GIVEN
        val message = "Тхис ис а ту лонг месач"

        // WHEN
        val splitMessage = message.splitMessageForIrc(15)

        // THEN
        assertEquals(listOf("Тхис ис", "а ту", "лонг", "месач"), splitMessage)
    }
}