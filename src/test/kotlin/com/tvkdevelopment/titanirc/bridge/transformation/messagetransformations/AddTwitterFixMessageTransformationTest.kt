package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AddTwitterFixMessageTransformationTest {

    private val sut = AddTwitterFixMessageTransformation()

    @Test
    fun testTransformation() {
        // GIVEN
        val message = "Hi https://twitter.com/bob/1234"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals("Hi https://vxtwitter.com/bob/1234", transformedMessage)
    }

    @Test
    fun testTransformationHttp() {
        // GIVEN
        val message = "Hi http://twitter.com/bob/1234"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals("Hi https://vxtwitter.com/bob/1234", transformedMessage)
    }

    @Test
    fun testTransformationWww() {
        // GIVEN
        val message = "Hi https://www.twitter.com/bob/1234"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals("Hi https://vxtwitter.com/bob/1234", transformedMessage)
    }

    @Test
    fun testNoDuplicatePrefix() {
        // GIVEN
        val message = "Hi https://vxtwitter.com/bob/1234"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals("Hi https://vxtwitter.com/bob/1234", transformedMessage)
    }
}