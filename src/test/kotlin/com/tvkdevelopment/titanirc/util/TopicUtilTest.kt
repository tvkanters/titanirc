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

    @Test
    fun testDoNotFilterNonJokeTopic() {
        // GIVEN
        val streamInfo = StreamInfo(
            streamer = "seagull",
            streamType = "game",
            title = "Normal Game 2"
        )

        // WHEN
        val result = TopicUtil.filterJokeTopics(streamInfo)

        // THEN
        assertEquals(streamInfo, result)
    }

    @Test
    fun testFilterJokeTopicMentioningSelf() {
        // GIVEN
        val streamInfo = StreamInfo(
            streamer = "seagull",
            streamType = "game",
            title = "seagull who is being a twat (Normal Game 2)"
        )

        // WHEN
        val result = TopicUtil.filterJokeTopics(streamInfo)

        // THEN
        assertEquals(streamInfo.copy(title = "Normal Game 2"), result)
    }

    @Test
    fun testFilterJokeTopicMentioningSelfIgnoresShortNames() {
        // GIVEN
        val streamInfo = StreamInfo(
            streamer = "a",
            streamType = "game",
            title = "a who is being a twat (Normal Game 2)"
        )

        // WHEN
        val result = TopicUtil.filterJokeTopics(streamInfo)

        // THEN
        assertEquals(streamInfo, result)
    }

    @Test
    fun testFilterJokeTopicNotMentioningSelf() {
        // GIVEN
        val streamInfo = StreamInfo(
            streamer = "seagull",
            streamType = "game",
            title = "some viewer who is being picked on because the stream is a bully (Normal Game 2)"
        )

        // WHEN
        val result = TopicUtil.filterJokeTopics(streamInfo)

        // THEN
        assertEquals(streamInfo.copy(title = "Normal Game 2"), result)
    }

    @Test
    fun testFilterJokeTopicNotMentioningSelfIgnoresShort() {
        // GIVEN
        val streamInfo = StreamInfo(
            streamer = "seagull",
            streamType = "game",
            title = "Some Title (Normal Game 2)"
        )

        // WHEN
        val result = TopicUtil.filterJokeTopics(streamInfo)

        // THEN
        assertEquals(streamInfo, result)
    }

    @Test
    fun testFilterJokeTopicIgnoresNonGames() {
        // GIVEN
        val streamInfo = StreamInfo(
            streamer = "seagull",
            streamType = "movie",
            title = "seagull movie 2 (the movieing)"
        )

        // WHEN
        val result = TopicUtil.filterJokeTopics(streamInfo)

        // THEN
        assertEquals(streamInfo, result)
    }
}