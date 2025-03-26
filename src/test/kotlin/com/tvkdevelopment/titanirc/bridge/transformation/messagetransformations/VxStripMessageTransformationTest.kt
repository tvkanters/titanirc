package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VxStripMessageTransformationTest {

    private val sut = VxStripMessageTransformation()

    @Test
    fun testInstagram() {
        // GIVEN
        val message = "Hi https://instagramez.com/bob/1234"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals("Hi https://instagram.com/bob/1234", transformedMessage)
    }

    @Test
    fun testInstagramHttp() {
        // GIVEN
        val message = "Hi http://instagramez.com/bob/1234"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals("Hi https://instagram.com/bob/1234", transformedMessage)
    }

    @Test
    fun testInstagramWww() {
        // GIVEN
        val message = "Hi https://www.instagramez.com/bob/1234"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals("Hi https://www.instagram.com/bob/1234", transformedMessage)
    }

    @Test
    fun testNoInstagramEz() {
        // GIVEN
        val message = "Hi https://instagram.com/bob/1234"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals("Hi https://instagram.com/bob/1234", transformedMessage)
    }
}