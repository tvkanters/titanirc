package com.tvkdevelopment.titanirc.util

object TopicUtil {

    private val REGEX = Regex(
        """^[^:]+: *(?<streamer>[^|]+?) *\| *(?<streamType>[^:]+?): *(?<title>[^|]+?) *(?:\||$)""",
        RegexOption.IGNORE_CASE
    )

    private val cache = LinkedHashMap<String, StreamInfo>()

    fun getStreamInfo(topic: String): StreamInfo? =
        cache[topic]
            ?: REGEX.find(topic)
                ?.let { match ->
                    val streamer = match.extractGroupValue("streamer") ?: return@let null
                    val streamType = match.extractGroupValue("streamType") ?: return@let null
                    val title = match.extractGroupValue("title") ?: return@let null
                    StreamInfo(streamer, streamType, title)
                        .also { cache.addWithLimit(topic, it, 3) }
                }

    private fun MatchResult.extractGroupValue(groupName: String): String? =
        groups[groupName]?.value?.takeIf { it.isNotBlank() }
}

data class StreamInfo(
    val streamer: String,
    val streamType: String,
    val title: String,
)
