package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.discord.MutableMemberRegistry
import com.tvkdevelopment.titanirc.niceMockk
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import io.mockk.every
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DiscordMemberToNicknameMessageTransformationTest {

    private val guild = mockkGuild(GUILD_ID, setOf(CHANNEL_ID))
    private val guildOther = mockkGuild(GUILD_ID_OTHER, setOf(CHANNEL_ID_OTHER))
    private val memberRegistry = MutableMemberRegistry().apply {
        add(guild, mockkMember(USER_ID, USER_NAME))
        add(guild, mockkMember(USER_ID_SPACE, USER_NAME_SPACE))
        add(guildOther, mockkMember(USER_ID_OTHER_GUILD, USER_NAME))
    }
    private val sut = DiscordMemberToNicknameMessageTransformation(memberRegistry)

    @Test
    fun testNoPing() {
        // GIVEN
        val message = "hello hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testName() {
        // GIVEN
        val message = "<@$USER_ID>, hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("$USER_NAME, hello", result)
    }

    @Test
    fun testNameUnknown() {
        // GIVEN
        val message = "<@00000>, hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("???, hello", result)
    }

    @Test
    fun testSpacedName() {
        // GIVEN
        val message = "<@$USER_ID_SPACE>, hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("$USER_NAME_SPACE, hello", result)
    }

    @Test
    fun testOtherGuild() {
        // GIVEN
        val message = "<@$USER_ID_OTHER_GUILD>: hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID_OTHER.toString(), "", message)

        // THEN
        assertEquals("$USER_NAME: hello", result)
    }

    @Test
    fun testOtherGuildNotFound() {
        // GIVEN
        val message = "<@$USER_ID_OTHER_GUILD>: hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("???: hello", result)
    }

    @Test
    fun testMultiple() {
        // GIVEN
        val message = "Hello <@$USER_ID>, <@$USER_ID_SPACE>, hi"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("Hello $USER_NAME, $USER_NAME_SPACE, hi", result)
    }

    @Test
    fun testUpdateName() {
        // GIVEN
        val message = "Hello, <@$USER_ID>"
        val preconditionResult = sut.transform(CHANNEL_ID.toString(), "", message)
        assertEquals("Hello, $USER_NAME", preconditionResult)

        // WHEN
        memberRegistry.add(guild, mockkMember(USER_ID, "Fred"))
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("Hello, Fred", result)
    }

    companion object {
        private const val GUILD_ID = 987L
        private const val GUILD_ID_OTHER = 654L

        private const val CHANNEL_ID = 111L
        private const val CHANNEL_ID_OTHER = 222L

        private const val USER_NAME = "Larry"
        private const val USER_NAME_SPACE = "Hans Bob"

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