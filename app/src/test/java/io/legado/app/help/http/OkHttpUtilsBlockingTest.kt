package io.legado.app.help.http

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class OkHttpUtilsBlockingTest {

    @Test
    fun blocking_helper_retries_and_succeeds_on_second_attempt() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(500).setBody("first"))
            server.enqueue(MockResponse().setResponseCode(200).setBody("second"))

            val client = OkHttpClient()
            val response = client.newCallResponseBlocking(retry = 1) {
                url(server.url("/retry"))
                get()
            }

            assertEquals(200, response.code)
            assertEquals("second", response.body.text())
            assertEquals(2, server.requestCount)
        }
    }

    @Test
    fun blocking_helper_returns_last_response_when_all_attempts_fail() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(500).setBody("first"))
            server.enqueue(MockResponse().setResponseCode(503).setBody("second"))

            val client = OkHttpClient()
            val response = client.newCallResponseBlocking(retry = 1) {
                url(server.url("/always-fail"))
                get()
            }

            assertEquals(503, response.code)
            assertEquals("second", response.body.text())
            assertEquals(2, server.requestCount)
        }
    }
}
