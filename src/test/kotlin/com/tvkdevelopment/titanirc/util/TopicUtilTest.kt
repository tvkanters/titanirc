package com.tvkdevelopment.titanirc.util

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TopicUtilTest {

    @Test
    fun testGetStreamInfo() {
        // GIVEN
        val topic = "Streamer: Some Streamer | Game: Some Game |"

        // WHEN
        val result = TopicUtil.getStreamInfo(topic)

        // THEN
        assertEquals(StreamInfo("Some Streamer", "Game", "Some Game"), result)
    }

    @Test
    fun testGetStreamInfoMissingEnd() {
        // GIVEN
        val topic = "Streamer: Some Streamer | Game: Some Game"

        // WHEN
        val result = TopicUtil.getStreamInfo(topic)

        // THEN
        assertEquals(StreamInfo("Some Streamer", "Game", "Some Game"), result)
    }

    @Test
    fun testGetStreamInfoLongerTopic() {
        // GIVEN
        val topic = "Streamer: Some Streamer | Game: Some Game | Longer topic"

        // WHEN
        val result = TopicUtil.getStreamInfo(topic)

        // THEN
        assertEquals(StreamInfo("Some Streamer", "Game", "Some Game"), result)
    }

    @Test
    fun testGetStreamInfoNoSpaces() {
        // GIVEN
        val topic = "Streamer:a|Game:b|"

        // WHEN
        val result = TopicUtil.getStreamInfo(topic)

        // THEN
        assertEquals(StreamInfo("a", "Game", "b"), result)
    }

    @Test
    fun testGetStreamInfoNoSpacesMissingEnd() {
        // GIVEN
        val topic = "Streamer:a|Game:b"

        // WHEN
        val result = TopicUtil.getStreamInfo(topic)

        // THEN
        assertEquals(StreamInfo("a", "Game", "b"), result)
    }

    @Test
    fun testGetStreamInfoMovie() {
        // GIVEN
        val topic = "Streamer: Some Streamer | Movie: Some Movie"

        // WHEN
        val result = TopicUtil.getStreamInfo(topic)

        // THEN
        assertEquals(StreamInfo("Some Streamer", "Movie", "Some Movie"), result)
    }

    @Test
    fun testGetStreamInfoNoStream() {
        // GIVEN
        val topic = "Streamer: | Game: |"

        // WHEN
        val result = TopicUtil.getStreamInfo(topic)

        // THEN
        assertNull(result)
    }

    @Test
    fun testGetStreamInfoNoStreamMissingEnd() {
        // GIVEN
        val topic = "Streamer: | Game:"

        // WHEN
        val result = TopicUtil.getStreamInfo(topic)

        // THEN
        assertNull(result)
    }

    @Test
    fun testGetStreamInfoEmpty() {
        // GIVEN
        val topic = ""

        // WHEN
        val result = TopicUtil.getStreamInfo(topic)

        // THEN
        assertNull(result)
    }
}