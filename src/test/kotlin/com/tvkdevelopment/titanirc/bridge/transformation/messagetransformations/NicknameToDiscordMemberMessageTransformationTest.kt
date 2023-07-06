package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.discord.MutableSnowflakeRegistry
import com.tvkdevelopment.titanirc.niceMockk
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import io.mockk.every
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NicknameToDiscordMemberMessageTransformationTest {

    private val guild = mockkGuild(GUILD_ID, setOf(CHANNEL_ID))
    private val guildOther = mockkGuild(GUILD_ID_OTHER, setOf(CHANNEL_ID_OTHER))
    private val snowflakeRegistry = MutableSnowflakeRegistry().apply {
        forGuild(guild).memberRegistry += mockkMember(USER_ID, USER_NAME)
        forGuild(guild).memberRegistry += mockkMember(USER_ID_SPACE, USER_NAME_SPACE)
        forGuild(guildOther).memberRegistry += mockkMember(USER_ID_OTHER_GUILD, USER_NAME)

        forGuild(guild).roleRegistry += mockkRole(ROLE_ID, ROLE_NAME)
    }
    private val sut = NicknameToDiscordMemberMessageTransformation(snowflakeRegistry)

    @Test
    fun testNoPing() {
        // GIVEN
        val message = "hello hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testNameSeparated() {
        // GIVEN
        val message = "$USER_NAME, hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<@$USER_ID>, hello", result)
    }

    @Test
    fun testNameNotSeparated() {
        // GIVEN
        val message = "$USER_NAME hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testNameLowercase() {
        // GIVEN
        val message = "${USER_NAME.lowercase()}, hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<@$USER_ID>, hello", result)
    }

    @Test
    fun testNameChunk() {
        // GIVEN
        val message = "${USER_NAME}arilla, hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testSpacedName() {
        // GIVEN
        val message = "$USER_NAME_SPACE_FIRST, hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<@$USER_ID_SPACE>, hello", result)
    }

    @Test
    fun testGarbageName() {
        // GIVEN
        val message = "$USER_NAME_GARBAGE hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testOtherGuild() {
        // GIVEN
        val message = "$USER_NAME: hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID_OTHER.toString(), message)

        // THEN
        assertEquals("<@$USER_ID_OTHER_GUILD>: hello", result)
    }

    @Test
    fun testNameMiddleOfSentence() {
        // GIVEN
        val message = "Hello $USER_NAME"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testNameMiddleOfSentenceWithComma() {
        // GIVEN
        val message = "Hello $USER_NAME, hi"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testAtName() {
        // GIVEN
        val message = "Hello @$USER_NAME"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("Hello <@$USER_ID>", result)
    }

    @Test
    fun testAtNameStart() {
        // GIVEN
        val message = "@$USER_NAME, hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<@$USER_ID>, hello", result)
    }

    @Test
    fun testAtNameCommas() {
        // GIVEN
        val message = "Hello @$USER_NAME, @$USER_NAME_SPACE_FIRST, hi"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("Hello <@$USER_ID>, <@$USER_ID_SPACE>, hi", result)
    }

    @Test
    fun testAtNameInWord() {
        // GIVEN
        val message = "Hello hi@$USER_NAME"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testAtNameInUrl() {
        // GIVEN
        val message = "Hello https://twitter.com/@$USER_NAME"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testAtNameGluedToCommas() {
        // GIVEN
        val message = "Hello,@$USER_NAME"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testUpdateName() {
        // GIVEN
        val message = "Hello, @$USER_NAME and @Fred"
        val preconditionResult = sut.transform("", CHANNEL_ID.toString(), message)
        assertEquals("Hello, <@$USER_ID> and @Fred", preconditionResult)

        // WHEN
        snowflakeRegistry.forGuild(guild).memberRegistry += mockkMember(USER_ID, "Fred")
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("Hello, <@$USER_ID> and <@$USER_ID>", result)
    }

    @Test
    fun testUpdateOverlapped() {
        // GIVEN a user takes another user's name
        val message = "Hello, @$USER_NAME and @$USER_NAME_SPACE_FIRST"
        sut.transform("", CHANNEL_ID.toString(), message)
        snowflakeRegistry.forGuild(guild).memberRegistry += mockkMember(USER_ID, USER_NAME_SPACE)
        sut.transform("", CHANNEL_ID.toString(), message)

        // WHEN the user reverts the name change
        snowflakeRegistry.forGuild(guild).memberRegistry += mockkMember(USER_ID, USER_NAME)
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN the stolen user's name is properly attributed again
        assertEquals("Hello, <@$USER_ID> and <@$USER_ID_SPACE>", result)
    }

    @Test
    fun testRoleAt() {
        // GIVEN
        val message = "@$ROLE_NAME"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<@&$ROLE_ID>", result)
    }

    @Test
    fun testRoleStart() {
        // GIVEN
        val message = "$ROLE_NAME, hi"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    companion object {
        private const val GUILD_ID = 987L
        private const val GUILD_ID_OTHER = 654L

        private const val CHANNEL_ID = 111L
        private const val CHANNEL_ID_OTHER = 222L

        private const val USER_ID = 123L
        private const val USER_ID_SPACE = 789L
        private const val USER_ID_OTHER_GUILD = 234L

        private const val USER_NAME = "Larry"
        private const val USER_NAME_SPACE = "Hans Bob"
        private const val USER_NAME_SPACE_FIRST = "Hans"
        private const val USER_NAME_GARBAGE = "%&^ABC"

        private const val ROLE_ID = 666L
        private const val ROLE_NAME = "Mods"

        fun mockkGuild(id: Long, channelIds: Set<Long>) = niceMockk<Guild> {
            every { this@niceMockk.id } returns Snowflake(id)
            every { this@niceMockk.channelIds } returns channelIds.map { Snowflake(it) }.toSet()
        }

        fun mockkMember(id: Long, name: String) = niceMockk<Member> {
            every { this@niceMockk.id } returns Snowflake(id)
            every { this@niceMockk.effectiveName } returns name
        }

        fun mockkRole(id: Long, name: String) = niceMockk<Role> {
            every { this@niceMockk.id } returns Snowflake(id)
            every { this@niceMockk.name } returns name
        }
    }
}