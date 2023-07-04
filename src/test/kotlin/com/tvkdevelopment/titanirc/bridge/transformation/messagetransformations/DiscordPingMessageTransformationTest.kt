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
        add(guild, mockkMember(ID_SHORT, NAME_SHORT))
        add(guild, mockkMember(ID_LONG, NAME_LONG))
        add(guild, mockkMember(ID_SPACE, NAME_SPACE))
        add(guildOther, mockkMember(ID_LONG_OTHER_GUILD, NAME_LONG))
    }
    private val sut = DiscordPingMessageTransformation(memberRegistry)

    @Test
    fun testNoPing() {
        // GIVEN
        val message = "hello hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("hello hello", result)
    }

    @Test
    fun testShortNameSeparated() {
        // GIVEN
        val message = "$NAME_SHORT, hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<@$ID_SHORT>, hello", result)
    }

    @Test
    fun testShortNameNotSeparated() {
        // GIVEN
        val message = "$NAME_SHORT hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("$NAME_SHORT hello", result)
    }

    @Test
    fun testLongNameSeparated() {
        // GIVEN
        val message = "$NAME_LONG: hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<@$ID_LONG>: hello", result)
    }

    @Test
    fun testLongNameNotSeparated() {
        // GIVEN
        val message = "$NAME_LONG hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<@$ID_LONG> hello", result)
    }

    @Test
    fun testSpacedName() {
        // GIVEN
        val message = "Hans hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("<@$ID_SPACE> hello", result)
    }

    @Test
    fun testGarbageName() {
        // GIVEN
        val message = "$NAME_GARBAGE hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID.toString(), message)

        // THEN
        assertEquals("$NAME_GARBAGE hello", result)
    }

    @Test
    fun testOtherGuild() {
        // GIVEN
        val message = "$NAME_LONG hello"

        // WHEN
        val result = sut.transform(CHANNEL_ID_OTHER.toString(), message)

        // THEN
        assertEquals("<@$ID_LONG_OTHER_GUILD> hello", result)
    }

    companion object {
        private const val GUILD_ID = 987L
        private const val GUILD_ID_OTHER = 654L

        private const val CHANNEL_ID = 111L
        private const val CHANNEL_ID_OTHER = 222L

        private const val NAME_SHORT = "a"
        private const val NAME_LONG = "Larry"
        private const val NAME_SPACE = "Hans Bob"
        private const val NAME_GARBAGE = "%&^ABC"

        private const val ID_SHORT = 123L
        private const val ID_LONG = 456L
        private const val ID_SPACE = 789L
        private const val ID_LONG_OTHER_GUILD = 234L

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