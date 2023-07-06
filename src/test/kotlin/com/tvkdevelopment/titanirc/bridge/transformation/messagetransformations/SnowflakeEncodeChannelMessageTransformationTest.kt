package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.discord.MutableSnowflakeRegistry
import com.tvkdevelopment.titanirc.niceMockk
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.Channel
import io.mockk.every
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SnowflakeEncodeChannelMessageTransformationTest {

    private val guild = mockkGuild(GUILD_ID, setOf(CHANNEL_ID))
    private val guildOther = mockkGuild(GUILD_ID_OTHER, setOf(CHANNEL_ID_OTHER))
    private val snowflakeRegistry = MutableSnowflakeRegistry().apply {
        forGuild(guild).channelRegistry += mockkChannel(CHANNEL_ID, CHANNEL_NAME)
        forGuild(guildOther).channelRegistry += mockkChannel(CHANNEL_ID_OTHER, CHANNEL_NAME_OTHER)
    }
    private val sut = SnowflakeEncodeChannelMessageTransformation(snowflakeRegistry)

    @Test
    fun testNoChannel() {
        // GIVEN
        val message = "hello hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testNameStart() {
        // GIVEN
        val message = "#$CHANNEL_NAME, hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<#$CHANNEL_ID>, hello", result)
    }

    @Test
    fun testNameMiddle() {
        // GIVEN
        val message = "Hello #$CHANNEL_NAME"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("Hello <#$CHANNEL_ID>", result)
    }

    @Test
    fun testNameChunk() {
        // GIVEN
        val message = "#${CHANNEL_NAME}arilla, hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testGarbageName() {
        // GIVEN
        val message = "$CHANNEL_NAME_GARBAGE hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testOtherGuild() {
        // GIVEN
        val message = "#$CHANNEL_NAME: hello"

        // WHEN
        val result = sut.transform("", CHANNEL_ID_OTHER.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testAtNameInUrl() {
        // GIVEN
        val message = "Hello https://twitter.com/#$CHANNEL_NAME"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testAtNameGluedToCommas() {
        // GIVEN
        val message = "Hello,#$CHANNEL_NAME"

        // WHEN
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals(message, result)
    }

    @Test
    fun testUpdateName() {
        // GIVEN
        val message = "Hello, #$CHANNEL_NAME and #lewd"
        val preconditionResult = sut.transform("", CHANNEL_ID.toString(), message)
        assertEquals("Hello, <#$CHANNEL_ID> and #lewd", preconditionResult)

        // WHEN
        snowflakeRegistry.forGuild(guild).channelRegistry += mockkChannel(CHANNEL_ID, "lewd")
        val result = sut.transform("", CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("Hello, <#$CHANNEL_ID> and <#$CHANNEL_ID>", result)
    }

    companion object {
        private const val GUILD_ID = 987L
        private const val GUILD_ID_OTHER = 654L

        private const val CHANNEL_ID = 111L
        private const val CHANNEL_ID_OTHER = 222L

        private const val CHANNEL_NAME = "freamonsmind"
        private const val CHANNEL_NAME_OTHER = "frezidsmind"
        private const val CHANNEL_NAME_GARBAGE = "%&^ABC"

        fun mockkGuild(id: Long, channelIds: Set<Long>) = niceMockk<Guild> {
            every { this@niceMockk.id } returns Snowflake(id)
            every { this@niceMockk.channelIds } returns channelIds.map { Snowflake(it) }.toSet()
        }

        fun mockkChannel(id: Long, name: String) = niceMockk<Channel> {
            every { this@niceMockk.id } returns Snowflake(id)
            every { this@niceMockk.data.name } returns Optional(name)
        }
    }
}