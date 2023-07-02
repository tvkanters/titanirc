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
        assertEquals("This is a\ntoo long\nmessage", splitMessage)
    }

    @Test
    fun testSplitMessageWithPrefix() {
        // GIVEN
        val message = "This is a too long message"

        // WHEN
        val splitMessage = message.splitMessageForIrc(12, prefix = "<a> ")

        // THEN
        assertEquals("<a> This is\n<a> a too\n<a> long\n<a> message", splitMessage)
    }

    @Test
    fun testSplitMessageWithPrefixAndNewlines() {
        // GIVEN
        val message = "This is a too long message\n\nwith new lines"

        // WHEN
        val splitMessage = message.splitMessageForIrc(12, prefix = "<a> ")

        // THEN
        assertEquals("<a> This is\n<a> a too\n<a> long\n<a> message\n<a> with new\n<a> lines", splitMessage)
    }

    @Test
    fun testSplitLongWord() {
        // GIVEN
        val message = "https://www.way.too/long"

        // WHEN
        val splitMessage = message.splitMessageForIrc(15)

        // THEN
        assertEquals("https://www.…", splitMessage)
    }

    @Test
    fun testSplitLongWordWithPrefix() {
        // GIVEN
        val message = "https://www.way.too/long"

        // WHEN
        val splitMessage = message.splitMessageForIrc(15, prefix = "<a> ")

        // THEN
        assertEquals("<a> https://…", splitMessage)
    }

    @Test
    fun testExtendedAscii() {
        // GIVEN
        val message = "Тхис ис а ту лонг месач"

        // WHEN
        val splitMessage = message.splitMessageForIrc(15)

        // THEN
        assertEquals("Тхис ис\nа ту\nлонг\nмесач", splitMessage)
    }
}