package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import org.junit.jupiter.api.Test
import org.pircbotx.Colors.*
import kotlin.test.assertEquals

class FormattingIrcToDiscordMessageTransformationTest {

    private val sut = FormattingIrcToDiscordMessageTransformation()

    @Test
    fun testBold() {
        // GIVEN
        val message = "${BOLD}test"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("**test**", result)
    }

    @Test
    fun testBoldMiddle() {
        // GIVEN
        val message = "t${BOLD}es${BOLD}t"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("t**es**t", result)
    }

    @Test
    fun testBoldClear() {
        // GIVEN
        val message = "t${BOLD}es${NORMAL}t"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("t**es**${NORMAL}t", result)
    }

    @Test
    fun testItalics() {
        // GIVEN
        val message = "${ITALICS}test"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("_test_", result)
    }

    @Test
    fun testItalicsMiddle() {
        // GIVEN
        val message = "t${ITALICS}es${ITALICS}t"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("t_es_t", result)
    }

    @Test
    fun testItalicsClear() {
        // GIVEN
        val message = "t${ITALICS}es${NORMAL}t"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("t_es_${NORMAL}t", result)
    }

    @Test
    fun testSpoilers() {
        // GIVEN
        val message = "\u00031,1test"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("||test||", result)
    }

    @Test
    fun testSpoilersMiddle() {
        // GIVEN
        val message = "t\u00031,1es\u00031,1t"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("t||es||t", result)
    }

    @Test
    fun testSpoilersClear() {
        // GIVEN
        val message = "t\u00031,1es${NORMAL}t"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("t||es||${NORMAL}t", result)
    }

    @Test
    fun testSpoilersClearWithEmptyColor() {
        // GIVEN
        val message = "t\u00031,1es\u0003t"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("t||es||\u0003t", result)
    }

    @Test
    fun testSpoilersClearWithDifferentColor() {
        // GIVEN
        val message = "t\u00031,1es\u00032,2t"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("t||es||\u00032,2t", result)
    }

    @Test
    fun testNested() {
        // GIVEN
        val message = "${ITALICS}te${BOLD}st"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("_te**st**_", result)
    }

    @Test
    fun testNestedMiddle() {
        // GIVEN
        val message = "t${ITALICS}e${BOLD}s${BOLD}t${ITALICS}s"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("t_e**s**t_s", result)
    }

    @Test
    fun testNestedAsymmetrical() {
        // GIVEN
        val message = "t${ITALICS}e${BOLD}s${ITALICS}t${BOLD}s"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("t_e**s**_**t**s", result)
    }

    @Test
    fun testHyperlink() {
        // GIVEN
        val message = "http://te${BOLD}st.com"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testHyperlinkBold() {
        // GIVEN
        val message = "http://test.com te${BOLD}st"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN no formatting is parsed
        assertEquals(message, result)
    }

    @Test
    fun testTrailingSpace() {
        // GIVEN
        val message = "${ITALICS}test ${ITALICS}spaces"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("_test_ spaces", result)
    }

    @Test
    fun testTrailingSpaces() {
        // GIVEN
        val message = "${ITALICS}test  $ITALICS spaces"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("_test_   spaces", result)
    }
}