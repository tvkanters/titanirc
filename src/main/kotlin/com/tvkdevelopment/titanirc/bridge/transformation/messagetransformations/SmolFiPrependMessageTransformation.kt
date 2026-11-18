package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.tvkdevelopment.titanirc.bridge.transformation.MessageTransformation
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder

class SmolFiPrependMessageTransformation(
    private val apiKey: String? = null,
    private val endpoint: URI = API_ENDPOINT,
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
        val connection = (endpoint.toURL().openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            instanceFollowRedirects = false
            connectTimeout = API_TIMEOUT_MILLIS
            readTimeout = API_TIMEOUT_MILLIS
            setRequestProperty("Authorization", "Bearer $key")
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
        }

        return try {
            connection.outputStream.use {
                it.write("""{"url":${JsonPrimitive(originalUrl)}}""".toByteArray(Charsets.UTF_8))
            }

            connection.takeIf { it.responseCode == 200 }
                ?.inputStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?.let { runCatching { Json.parseToJsonElement(it) }.getOrNull() }
                ?.let { it as? JsonObject }
                ?.get("short")
                ?.let { it as? JsonPrimitive }
                ?.takeIf { it.isString }
                ?.contentOrNull
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private val API_ENDPOINT = URI.create("https://e.smol.fi/api/s")
        private const val API_TIMEOUT_MILLIS = 2000

        private val REGEX =
            Regex("""(?<!https://smol.fi/e/\?v=)https?://(?:media|cdn)\.discordapp\.(?:net|com)/attachments/((?:[^/\s?]+/)*([^/\s?]+)(?:\?\S*)?)""")
        private val LEGACY_VIDEO_EXTENSION = Regex("""\.(?:mp4|webm|mov)$""")

        private fun encode(string: String) = URLEncoder.encode(string, Charsets.UTF_8.name())
    }
}
