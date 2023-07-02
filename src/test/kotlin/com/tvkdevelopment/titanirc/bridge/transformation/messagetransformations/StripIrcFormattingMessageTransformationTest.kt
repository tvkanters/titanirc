package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import org.junit.jupiter.api.Test
import org.pircbotx.Colors
import kotlin.test.assertEquals

class StripIrcFormattingMessageTransformationTest {

    private val sut = StripIrcFormattingMessageTransformation()

    @Test
    fun testTransformation() {
        // GIVEN
        val message = "mes${Colors.BOLD}s${Colors.BLUE}a${Colors.NORMAL}ge"

        // WHEN
        val transformedMessage = sut.transform(message)

        // THEN
        assertEquals("message", transformedMessage)
    }
}