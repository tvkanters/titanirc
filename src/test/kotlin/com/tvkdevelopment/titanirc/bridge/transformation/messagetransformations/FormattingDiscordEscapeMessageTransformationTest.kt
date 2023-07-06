package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FormattingDiscordEscapeMessageTransformationTest {

    private val sut = FormattingDiscordEscapeMessageTransformation()

    @Test
    fun testEscape() {
        // GIVEN
        val message = "*t_e~s`t\\"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("\\*t\\_e\\~s\\`t\\\\", result)
    }

    @Test
    fun testDoNotEscapeHyperlinks() {
        // GIVEN
        val message = "_test_ http://te-st.com/a_2 *test*"

        // WHEN
        val result = sut.transform("", "", message)

        // THEN
        assertEquals("\\_test\\_ http://te-st.com/a_2 \\*test\\*", result)
    }
}