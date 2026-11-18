package com.tvkdevelopment.titanirc.bridge.transformation.messagetransformations

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SmolFiPrependMessageTransformationTest {

    private val sut = SmolFiPrependMessageTransformation()

    @Test
    fun testTransformation() {
        // GIVEN
        val message = "Hi https://media.discordapp.net/attachments/1234/1234/test.mp4"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals(
            "Hi https://smol.fi/e/?v=media.discordapp.net%2Fattachments%2F1234%2F1234%2Ftest.mp4",
            transformedMessage
        )
    }

    @Test
    fun testTransformationHttp() {
        // GIVEN
        val message = "Hi http://media.discordapp.net/attachments/1234/1234/test.mp4"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals(
            "Hi https://smol.fi/e/?v=media.discordapp.net%2Fattachments%2F1234%2F1234%2Ftest.mp4",
            transformedMessage
        )
    }

    @Test
    fun testTransformationCdn() {
        // GIVEN
        val message = "Hi https://cdn.discordapp.com/attachments/1234/1234/test.mp4"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals(
            "Hi https://smol.fi/e/?v=media.discordapp.net%2Fattachments%2F1234%2F1234%2Ftest.mp4",
            transformedMessage
        )
    }

    @Test
    fun testNoDuplicateSmolFi() {
        // GIVEN
        val message = "Hi https://smol.fi/e/?v=media.discordapp.net/attachments/1234/1234/test.mp4"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals(
            "Hi https://smol.fi/e/?v=media.discordapp.net/attachments/1234/1234/test.mp4",
            transformedMessage
        )
    }

    @Test
    fun testNoDuplicateSmolFiWithHttps() {
        // GIVEN
        val message = "Hi https://smol.fi/e/?v=https://media.discordapp.net/attachments/1234/1234/test.mp4"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals(
            "Hi https://smol.fi/e/?v=https://media.discordapp.net/attachments/1234/1234/test.mp4",
            transformedMessage
        )
    }

    @Test
    fun testNoSmolFiNonVideo() {
        // GIVEN
        val message = "Hi https://media.discordapp.net/attachments/1234/1234/test.jpg no .mp4"

        // WHEN
        val transformedMessage = sut.transform("", "", message)

        // THEN
        assertEquals(
            "Hi https://media.discordapp.net/attachments/1234/1234/test.jpg no .mp4",
            transformedMessage
        )
    }

    @Test
    fun testNoSmolFiExtensionlessAttachment() {
        val message = "Hi https://cdn.discordapp.com/attachments/1234/1234/file"

        assertEquals(
            message,
            sut.transform("", "", message)
        )
    }

    @Test
    fun testApiShortensOriginalUrlWithoutEncoding() {
        val original = "https://cdn.discordapp.com/attachments/1234/1234/test.mp4?ex=123&hm=a%2Fb"
        val method = AtomicReference<String>()
        val authorization = AtomicReference<String>()
        val contentType = AtomicReference<String>()
        val requestBody = AtomicReference<String>()

        val result = transformWithServer(
            "Hi $original",
            """{"id":"sSQesjJ","short":"https://e.smol.fi/v/sSQesjJ"}""",
        ) { request ->
            method.set(request.requestMethod)
            authorization.set(request.requestHeaders.getFirst("Authorization"))
            contentType.set(request.requestHeaders.getFirst("Content-Type"))
            requestBody.set(request.requestBody.bufferedReader().use { it.readText() })
        }

        assertEquals("Hi https://e.smol.fi/v/sSQesjJ?file=test.mp4", result)
        assertEquals("POST", method.get())
        assertEquals("Bearer test-api-key", authorization.get())
        assertEquals("application/json", contentType.get())
        assertEquals("""{"url":"$original"}""", requestBody.get())
    }

    @Test
    fun testApiRejectsPlainTextResponse() {
        val result = transformWithServer(
            "https://media.discordapp.net/attachments/1234/1234/test.webm",
            "https://e.smol.fi/video123",
        )

        assertEquals("https://smol.fi/e/?v=media.discordapp.net%2Fattachments%2F1234%2F1234%2Ftest.webm", result)
    }

    @Test
    fun testApiRejectsJsonStringResponse() {
        val result = transformWithServer(
            "https://media.discordapp.net/attachments/1234/1234/test.mov",
            """"https://e.smol.fi/video123"""",
        )

        assertEquals("https://smol.fi/e/?v=media.discordapp.net%2Fattachments%2F1234%2F1234%2Ftest.mov", result)
    }

    @Test
    fun testApiShortensNonVideoAttachment() {
        val result = transformWithServer(
            "https://media.discordapp.net/attachments/1234/1234/image.png",
            """{"id":"image123","short":"https://e.smol.fi/v/image123"}""",
        )

        assertEquals("https://e.smol.fi/v/image123?file=image.png", result)
    }

    @Test
    fun testApiShortensExtensionlessAttachment() {
        val result = transformWithServer(
            "https://cdn.discordapp.com/attachments/1234/1234/file",
            """{"short":"https://e.smol.fi/v/file123"}""",
        )

        assertEquals("https://e.smol.fi/v/file123?file=file", result)
    }

    @Test
    fun testApiPreservesExistingShortUrlParameters() {
        val result = transformWithServer(
            "https://media.discordapp.net/attachments/1234/1234/image.png?ex=123",
            """{"short":"https://e.smol.fi/v/image123?existing=value"}""",
        )

        assertEquals("https://e.smol.fi/v/image123?existing=value", result)
    }

    @Test
    fun testApiFailureLeavesNonVideoUnchanged() {
        val original = "https://media.discordapp.net/attachments/1234/1234/image.jpg?ex=123"

        assertEquals(original, transformWithServer(original, "failed", status = 500))
    }

    @Test
    fun testApiFailureFallsBackToOriginalBehaviour() {
        val original = "http://cdn.discordapp.com/attachments/1234/1234/test.mp4?ex=123&hm=abc"
        val fallback = "https://smol.fi/e/?v=media.discordapp.net%2Fattachments%2F1234%2F1234%2Ftest.mp4%3Fex%3D123%26hm%3Dabc"

        assertEquals(fallback, transformWithServer(original, "failed", status = 500))
        assertEquals(fallback, transformWithServer(original, """{"short":"https://e.smol.fi/v/other"}""", status = 201))
        assertEquals(fallback, transformWithServer(original, "{\"error\":\"not found\"}"))
        assertEquals(fallback, transformWithServer(original, "https://example.com/unrelated"))
        assertEquals(fallback, transformWithServer(original, """{"short_url":"https://e.smol.fi/v/old"}"""))
        assertEquals(fallback, transformWithServer(original, """{"url":"https://e.smol.fi/v/old"}"""))
        assertEquals(fallback, transformWithServer(original, """{"short":42}"""))
    }

    @Test
    fun testApiTimesOutAfterTwoSeconds() {
        val original = "https://media.discordapp.net/attachments/1234/1234/test.mp4"
        val start = System.nanoTime()

        val result = transformWithServer(original, "https://e.smol.fi/late", delayMillis = 4000)

        assertEquals("https://smol.fi/e/?v=media.discordapp.net%2Fattachments%2F1234%2F1234%2Ftest.mp4", result)
        assertTrue((System.nanoTime() - start) / 1_000_000 < 3000, "API request exceeded the 2-second timeout")
    }

    private fun transformWithServer(
        message: String,
        response: String,
        status: Int = 200,
        delayMillis: Long = 0,
        onRequest: (HttpExchange) -> Unit = {},
    ): String {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/api/s") { exchange ->
            exchange.use {
                onRequest(exchange)
                if (delayMillis > 0) Thread.sleep(delayMillis)
                val bytes = response.toByteArray(Charsets.UTF_8)
                exchange.sendResponseHeaders(status, bytes.size.toLong())
                exchange.responseBody.write(bytes)
            }
        }
        server.start()

        return try {
            SmolFiPrependMessageTransformation(
                "test-api-key",
                URI.create("http://127.0.0.1:${server.address.port}/api/s"),
            ).transform("", "", message)
        } finally {
            server.stop(0)
        }
    }
}
