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
    private val endpoint: URI = URI.create("https://e.smol.fi/api/s"),
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(3))
        .build(),
) : MessageTransformation {

    override fun transform(sourceChannel: String, targetChannel: String, message: String): String =
        message.replace(REGEX) { match ->
            val fallback = "https://smol.fi/e/?v=media.discordapp.net%2Fattachments%2F${encode(match.groupValues[1])}"
            val key = apiKey?.takeIf { it.isNotBlank() } ?: return@replace fallback

            try {
                shorten(key, match.value) ?: fallback
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                fallback
            } catch (_: Exception) {
                fallback
            }
        }

    private fun shorten(key: String, originalUrl: String): String? {
        val request = HttpRequest.newBuilder(endpoint)
            .timeout(Duration.ofSeconds(3))
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("""{"url":${JsonPrimitive(originalUrl)}}"""))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) return null

        val shortUrl = try {
            val json = Json.parseToJsonElement(response.body()) as? JsonObject
            (json?.get("short") as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull
        } catch (_: Exception) {
            null
        }

        return shortUrl?.takeIf { isValidShortUrl(it) }
    }

    companion object {
        private val REGEX =
            Regex("""(?<!https://smol.fi/e/\?v=)https?://(?:media|cdn)\.discordapp\.(?:net|com)/attachments/(\S+)""")

        private fun encode(string: String) = URLEncoder.encode(string, Charsets.UTF_8.name())

        private fun isValidShortUrl(value: String): Boolean = try {
            val uri = URI(value)
            uri.scheme == "https" &&
                (uri.host == "smol.fi" || uri.host?.endsWith(".smol.fi") == true) &&
                !uri.rawPath.isNullOrBlank() && uri.userInfo == null && uri.rawFragment == null
        } catch (_: Exception) {
            false
        }
    }
}
