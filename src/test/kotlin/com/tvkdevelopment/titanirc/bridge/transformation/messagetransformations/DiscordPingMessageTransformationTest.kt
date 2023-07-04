package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.discord.MutableMemberRegistry
import com.tvkdevelopment.titanirc.niceMockk
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import io.mockk.every
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DiscordPingMessageTransformationTest {

    private val guild = mockkGuild(GUILD_ID, setOf(CHANNEL_ID))
    private val guildOther = mockkGuild(GUILD_ID_OTHER, setOf(CHANNEL_ID_OTHER))
    private val memberRegistry = MutableMemberRegistry().apply {
        add(guild, mockkMember(USER_ID, USER_NAME))
        add(guild, mockkMember(USER_ID_SPACE, USER_NAME_SPACE))
        add(guildOther, mockkMember(USER_ID_OTHER_GUILD, USER_NAME))
    }
    private val sut = DiscordPingMessageTransformation(memberRegistry)

    @Test
    fun testNoPing() {
        // GIVEN
        val message = "hello hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testNameSeparated() {
        // GIVEN
        val message = "$USER_NAME, hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<@$USER_ID>, hello", result)
    }

    @Test
    fun testNameNotSeparated() {
        // GIVEN
        val message = "$USER_NAME hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testNameLowercase() {
        // GIVEN
        val message = "${USER_NAME.lowercase()}, hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<@$USER_ID>, hello", result)
    }

    @Test
    fun testNameChunk() {
        // GIVEN
        val message = "${USER_NAME}arilla, hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testSpacedName() {
        // GIVEN
        val message = "$USER_NAME_SPACE_FIRST, hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<@$USER_ID_SPACE>, hello", result)
    }

    @Test
    fun testGarbageName() {
        // GIVEN
        val message = "$USER_NAME_GARBAGE hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testOtherGuild() {
        // GIVEN
        val message = "$USER_NAME: hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID_OTHER.toString(), message)

        // THEN
        assertEquals("<@$USER_ID_OTHER_GUILD>: hello", result)
    }

    @Test
    fun testNameMiddleOfSentence() {
        // GIVEN
        val message = "Hello $USER_NAME"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testNameMiddleOfSentenceWithComma() {
        // GIVEN
        val message = "Hello $USER_NAME, hi"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testAtName() {
        // GIVEN
        val message = "Hello @$USER_NAME"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("Hello <@$USER_ID>", result)
    }

    @Test
    fun testAtNameStart() {
        // GIVEN
        val message = "@$USER_NAME, hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<@$USER_ID>, hello", result)
    }

    @Test
    fun testAtNameCommas() {
        // GIVEN
        val message = "Hello @$USER_NAME, @$USER_NAME_SPACE_FIRST, hi"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("Hello <@$USER_ID>, <@$USER_ID_SPACE>, hi", result)
    }

    @Test
    fun testAtNameInWord() {
        // GIVEN
        val message = "Hello hi@$USER_NAME"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testAtNameInUrl() {
        // GIVEN
        val message = "Hello https://twitter.com/@$USER_NAME"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testAtNameGluedToCommas() {
        // GIVEN
        val message = "Hello,@$USER_NAME"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    companion object {
        private const val GUILD_ID = 987L
        private const val GUILD_ID_OTHER = 654L

        private const val CHANNEL_ID = 111L
        private const val CHANNEL_ID_OTHER = 222L

        private const val USER_NAME = "Larry"
        private const val USER_NAME_SPACE = "Hans Bob"
        private const val USER_NAME_SPACE_FIRST = "Hans"
        private const val USER_NAME_GARBAGE = "%&^ABC"

        private const val USER_ID = 123L
        private const val USER_ID_SPACE = 789L
        private const val USER_ID_OTHER_GUILD = 234L

        fun mockkGuild(id: Long, channelIds: Set<Long>) = niceMockk<Guild> {
            every { this@niceMockk.id } returns Snowflake(id)
            every { this@niceMockk.channelIds } returns channelIds.map { Snowflake(it) }.toSet()
        }

        fun mockkMember(id: Long, name: String) = niceMockk<Member> {
            every { this@niceMockk.id } returns Snowflake(id)
            every { this@niceMockk.effectiveName } returns name
        }
    }
}