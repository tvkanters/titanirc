package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AddSmolFiMessageTransformationTest {

    private val sut = AddSmolFiMessageTransformation()

    @Test
    fun testTransformation() {
        // GIVEN
        val message = "Hi https://media.discordapp.net/attachments/1234/1234/test.mp4"

        // WHEN
        val transformedMessage = sut.transform(message)

        // THEN
        assertEquals(
            "Hi https://smol.fi/e/?v=https://media.discordapp.net/attachments/1234/1234/test.mp4",
            transformedMessage
        )
    }

    @Test
    fun testTransformationHttp() {
        // GIVEN
        val message = "Hi http://media.discordapp.net/attachments/1234/1234/test.mp4"

        // WHEN
        val transformedMessage = sut.transform(message)

        // THEN
        assertEquals(
            "Hi https://smol.fi/e/?v=https://media.discordapp.net/attachments/1234/1234/test.mp4",
            transformedMessage
        )
    }

    @Test
    fun testTransformationCdn() {
        // GIVEN
        val message = "Hi https://cdn.discordapp.com/attachments/1234/1234/test.mp4"

        // WHEN
        val transformedMessage = sut.transform(message)

        // THEN
        assertEquals(
            "Hi https://smol.fi/e/?v=https://media.discordapp.net/attachments/1234/1234/test.mp4",
            transformedMessage
        )
    }

    @Test
    fun testNoDuplicateSmolFi() {
        // GIVEN
        val message = "Hi https://smol.fi/e/?v=https://media.discordapp.net/attachments/1234/1234/test.mp4"

        // WHEN
        val transformedMessage = sut.transform(message)

        // THEN
        assertEquals(
            "Hi https://smol.fi/e/?v=https://media.discordapp.net/attachments/1234/1234/test.mp4",
            transformedMessage
        )
    }

    @Test
    fun testNoSmolFiNonVideo() {
        // GIVEN
        val message = "Hi https://media.discordapp.net/attachments/1234/1234/test.jpg no .mp4"

        // WHEN
        val transformedMessage = sut.transform(message)

        // THEN
        assertEquals(
            "Hi https://media.discordapp.net/attachments/1234/1234/test.jpg no .mp4",
            transformedMessage
        )
    }
}