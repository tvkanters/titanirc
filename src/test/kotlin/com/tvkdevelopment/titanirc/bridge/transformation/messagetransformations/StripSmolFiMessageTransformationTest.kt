package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StripSmolFiMessageTransformationTest {

    @Test
    fun testTransformationHttps() {
        // GIVEN
        val sut = StripSmolFiMessageTransformation()
        val message = "Hi https://smol.fi/e/?v=http://google.com?test"

        // WHEN
        val transformedMessage = sut.transform(message)

        // THEN
        assertEquals("Hi http://google.com?test", transformedMessage)
    }

    @Test
    fun testTransformationHttp() {
        // GIVEN
        val sut = StripSmolFiMessageTransformation()
        val message = "Hi http://smol.fi/e/?v=https://google.com?test"

        // WHEN
        val transformedMessage = sut.transform(message)

        // THEN
        assertEquals("Hi https://google.com?test", transformedMessage)
    }
}