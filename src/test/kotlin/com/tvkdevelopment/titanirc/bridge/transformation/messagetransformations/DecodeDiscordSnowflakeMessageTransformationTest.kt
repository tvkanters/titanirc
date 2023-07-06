package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.discord.MutableSnowflakeRegistry
import com.tvkdevelopment.titanirc.niceMockk
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.Role
import dev.kord.core.entity.channel.Channel
import io.mockk.every
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DecodeDiscordSnowflakeMessageTransformationTest {

    private val guild = mockkGuild(GUILD_ID, setOf(CHANNEL_ID))
    private val guildOther = mockkGuild(GUILD_ID_OTHER, setOf(CHANNEL_ID_OTHER))
    private val snowflakeRegistry = MutableSnowflakeRegistry().apply {
        forGuild(guild).channelRegistry += mockkChannel(CHANNEL_ID, CHANNEL_NAME)
        forGuild(guildOther).channelRegistry += mockkChannel(CHANNEL_ID_OTHER, CHANNEL_NAME_OTHER)

        forGuild(guild).memberRegistry += mockkMember(USER_ID, USER_NAME)
        forGuild(guild).memberRegistry += mockkMember(USER_ID_SPACE, USER_NAME_SPACE)
        forGuild(guildOther).memberRegistry += mockkMember(USER_ID_OTHER_GUILD, USER_NAME)

        forGuild(guild).roleRegistry += mockkRole(ROLE_ID, ROLE_NAME)
    }
    private val sut = DecodeDiscordSnowflakeMessageTransformation(snowflakeRegistry)

    @Test
    fun testNoSnowflake() {
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
    fun testNameOtherGuild() {
        // GIVEN
        val message = "<@$USER_ID_OTHER_GUILD>: hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID_OTHER.toString(), "", message)

        // THEN
        assertEquals("$USER_NAME: hello", result)
    }

    @Test
    fun testNameOtherGuildNotFound() {
        // GIVEN
        val message = "<@$USER_ID_OTHER_GUILD>: hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("???: hello", result)
    }

    @Test
    fun testMultipleNames() {
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
        snowflakeRegistry.forGuild(guild).memberRegistry += mockkMember(USER_ID, "Fred")
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("Hello, Fred", result)
    }

    @Test
    fun testChannel() {
        // GIVEN
        val message = "<#$CHANNEL_ID>, hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("#$CHANNEL_NAME, hello", result)
    }

    @Test
    fun testChannelUnknown() {
        // GIVEN
        val message = "<#00000>, hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("???, hello", result)
    }

    @Test
    fun testChannelOtherGuild() {
        // GIVEN
        val message = "<#$CHANNEL_ID_OTHER>: hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("???: hello", result)
    }

    @Test
    fun testChannelOtherGuildNotFound() {
        // GIVEN
        val message = "<#$CHANNEL_ID_OTHER>: hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("???: hello", result)
    }

    @Test
    fun testMultipleChannels() {
        // GIVEN
        val message = "Hello <#$CHANNEL_ID>, <#$CHANNEL_ID_OTHER>, hi"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("Hello #$CHANNEL_NAME, ???, hi", result)
    }

    @Test
    fun testUpdateChannel() {
        // GIVEN
        val message = "Hello, <#$CHANNEL_ID>"
        val preconditionResult = sut.transform(CHANNEL_ID.toString(), "", message)
        assertEquals("Hello, #$CHANNEL_NAME", preconditionResult)

        // WHEN
        snowflakeRegistry.forGuild(guild).channelRegistry += mockkChannel(CHANNEL_ID, "lewd")
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("Hello, #lewd", result)
    }

    @Test
    fun testEmoji() {
        // GIVEN
        val message = "<:pepe:123>"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals(":pepe:", result)
    }

    @Test
    fun testAnimatedEmoji() {
        // GIVEN
        val message = "<a:pepe:123>"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals(":pepe:", result)
    }

    @Test
    fun testRole() {
        // GIVEN
        val message = "<@&$ROLE_ID>"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), "", message)

        // THEN
        assertEquals("@$ROLE_NAME", result)
    }

    companion object {
        private const val GUILD_ID = 987L
        private const val GUILD_ID_OTHER = 654L

        private const val CHANNEL_ID = 111L
        private const val CHANNEL_ID_OTHER = 222L

        private const val CHANNEL_NAME = "freamonsmind"
        private const val CHANNEL_NAME_OTHER = "frezidsmind"

        private const val USER_ID = 123L
        private const val USER_ID_SPACE = 789L
        private const val USER_ID_OTHER_GUILD = 234L

        private const val USER_NAME = "Larry"
        private const val USER_NAME_SPACE = "Hans Bob"

        private const val ROLE_ID = 666L
        private const val ROLE_NAME = "Mods"

        fun mockkGuild(id: Long, channelIds: Set<Long>) = niceMockk<Guild> {
            every { this@niceMockk.id } returns Snowflake(id)
            every { this@niceMockk.channelIds } returns channelIds.map { Snowflake(it) }.toSet()
        }

        fun mockkChannel(id: Long, name: String) = niceMockk<Channel> {
            every { this@niceMockk.id } returns Snowflake(id)
            every { this@niceMockk.data.name } returns Optional(name)
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