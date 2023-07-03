package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import org.junit.jupiter.api.Test
import org.pircbotx.Colors
import kotlin.test.assertEquals

class ConvertIrcFormattingToDiscordMessageTransformationTest {

    private val sut = ConvertIrcFormattingToDiscordMessageTransformation()

    @Test
    fun testBold() {
        // GIVEN
        val message = "${Colors.BOLD}test"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("**test**", result)
    }

    @Test
    fun testBoldMiddle() {
        // GIVEN
        val message = "t${Colors.BOLD}es${Colors.BOLD}t"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("t**es**t", result)
    }

    @Test
    fun testBoldClear() {
        // GIVEN
        val message = "t${Colors.BOLD}es${Colors.NORMAL}t"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("t**es**${Colors.NORMAL}t", result)
    }

    @Test
    fun testItalics() {
        // GIVEN
        val message = "${Colors.ITALICS}test"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("_test_", result)
    }

    @Test
    fun testItalicsMiddle() {
        // GIVEN
        val message = "t${Colors.ITALICS}es${Colors.ITALICS}t"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("t_es_t", result)
    }

    @Test
    fun testItalicsClear() {
        // GIVEN
        val message = "t${Colors.ITALICS}es${Colors.NORMAL}t"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("t_es_${Colors.NORMAL}t", result)
    }

    @Test
    fun testSpoilers() {
        // GIVEN
        val message = "\u00031,1test"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("||test||", result)
    }

    @Test
    fun testSpoilersMiddle() {
        // GIVEN
        val message = "t\u00031,1es\u00031,1t"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("t||es||t", result)
    }

    @Test
    fun testSpoilersClear() {
        // GIVEN
        val message = "t\u00031,1es${Colors.NORMAL}t"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("t||es||${Colors.NORMAL}t", result)
    }

    @Test
    fun testSpoilersClearWithEmptyColor() {
        // GIVEN
        val message = "t\u00031,1es\u0003t"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("t||es||\u0003t", result)
    }

    @Test
    fun testSpoilersClearWithDifferentColor() {
        // GIVEN
        val message = "t\u00031,1es\u00032,2t"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("t||es||\u00032,2t", result)
    }

    @Test
    fun testNested() {
        // GIVEN
        val message = "${Colors.ITALICS}te${Colors.BOLD}st"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("_te**st**_", result)
    }

    @Test
    fun testNestedMiddle() {
        // GIVEN
        val message = "t${Colors.ITALICS}e${Colors.BOLD}s${Colors.BOLD}t${Colors.ITALICS}s"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("t_e**s**t_s", result)
    }

    @Test
    fun testNestedAsymmtrical() {
        // GIVEN
        val message = "t${Colors.ITALICS}e${Colors.BOLD}s${Colors.ITALICS}t${Colors.BOLD}s"

        // WHEN
        val result = sut.transform("", message)

        // THEN
        assertEquals("t_e**s**_**t**s", result)
    }
}