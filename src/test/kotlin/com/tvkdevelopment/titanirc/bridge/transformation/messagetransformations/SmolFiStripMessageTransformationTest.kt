package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SmolFiStripMessageTransformationTest {

    private val sut = SmolFiStripMessageTransformation()

    @Test
    fun testTransformationHttps() {
        // GIVEN
        val message = "Hi https://smol.fi/e/?v=https://google.com?test"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals("Hi https://google.com?test", transformedMessage)
    }

    @Test
    fun testTransformationHttp() {
        // GIVEN
        val message = "Hi http://smol.fi/e/?v=http://google.com?test"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals("Hi https://google.com?test", transformedMessage)
    }

    @Test
    fun testTransformationNoProtocol() {
        // GIVEN
        val message = "Hi http://smol.fi/e/?v=google.com?test"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals("Hi https://google.com?test", transformedMessage)
    }
}