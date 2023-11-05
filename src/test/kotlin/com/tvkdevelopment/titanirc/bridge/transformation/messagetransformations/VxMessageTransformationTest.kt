package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VxMessageTransformationTest {

    private val sut = VxMessageTransformation()

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

    @Test
    fun testRedditTransformation() {
        // GIVEN
        val message = "Hi https://reddit.com/r/somepost"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals("Hi https://rxddit.com/r/somepost", transformedMessage)
    }

    @Test
    fun testOldRedditTransformation() {
        // GIVEN
        val message = "Hi https://old.reddit.com/r/somepost"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals("Hi https://old.rxddit.com/r/somepost", transformedMessage)
    }
}