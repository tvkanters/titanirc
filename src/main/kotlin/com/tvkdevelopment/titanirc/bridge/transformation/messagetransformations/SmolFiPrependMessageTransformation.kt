package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class SmolFiPrependMessageTransformation(
    private val apiKey: String? = null,
    private val endpoint: URI = API_ENDPOINT,
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(API_TIMEOUT)
        .build(),
) : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message.replace(REGEX) { match ->
            apiKey?.let { key ->
                runCatching { shorten(key, match.value) }
                    .onFailure { if (it is InterruptedException) Thread.currentThread().interrupt() }
                    .getOrNull()
                    ?.let { short ->
                        if ('?' in short) short else "$short?file=${match.groupValues[2]}"
                    }
            } ?: match.takeIf { LEGACY_VIDEO_EXTENSION.containsMatchIn(it.groupValues[2]) }
                ?.let { "https://smol.fi/e/?v=media.discordapp.net%2Fattachments%2F${encode(it.groupValues[1])}" }
                ?: match.value
        }

    private fun shorten(key: String, originalUrl: String): String? {
        val request = HttpRequest.newBuilder(endpoint)
            .timeout(API_TIMEOUT)
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("""{"url":${JsonPrimitive(originalUrl)}}"""))
            .build()

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            .takeIf { it.statusCode() == 200 }
            ?.body()
            ?.let { runCatching { Json.parseToJsonElement(it) }.getOrNull() }
            ?.let { it as? JsonObject }
            ?.get("short")
            ?.let { it as? JsonPrimitive }
            ?.takeIf { it.isString }
            ?.contentOrNull
    }

    companion object {
        private val API_ENDPOINT = URI.create("https://e.smol.fi/api/s")
        private val API_TIMEOUT = Duration.ofSeconds(2)

        private val REGEX =
            Regex("""(?<!https://smol.fi/e/\?v=)https?://(?:media|cdn)\.discordapp\.(?:net|com)/attachments/((?:[^/\s?]+/)*([^/\s?]+)(?:\?\S*)?)""")
        private val LEGACY_VIDEO_EXTENSION = Regex("""\.(?:mp4|webm|mov)$""")

        private fun encode(string: String) = URLEncoder.encode(string, Charsets.UTF_8.name())
    }
}
