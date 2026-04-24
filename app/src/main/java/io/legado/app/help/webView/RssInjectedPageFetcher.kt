package io.legado.app.help.webView

import io.legado.app.ui.rss.read.RssHtmlHeadInjector
import java.nio.charset.Charset

internal data class RssInjectedPageRequest(
    val url: String,
    val method: String,
    val requestHeaders: Map<String, String>,
    val cookie: String?
)

internal data class RssInjectedPageHttpResponse(
    val statusCode: Int,
    val mimeType: String,
    val charset: Charset,
    val contentType: String?,
    val bodyText: String,
    val setCookies: List<String>
)

internal data class RssInjectedPageResult(
    val statusCode: Int,
    val mimeType: String,
    val charset: Charset,
    val contentType: String?,
    val bodyText: String,
    val setCookies: List<String>
)

internal data class RssInjectedPageFetchOutcome(
    val result: RssInjectedPageResult?,
    val failureType: String?
)

internal object RssInjectedPageFetcher {

    fun fetch(
        url: String,
        method: String,
        requestHeaders: Map<String, String>,
        cookie: String?,
        snippet: String,
        executeRequest: (RssInjectedPageRequest) -> RssInjectedPageHttpResponse
    ): RssInjectedPageFetchOutcome {
        return try {
            val request = RssInjectedPageRequest(
                url = url,
                method = method,
                requestHeaders = requestHeaders,
                cookie = cookie
            )
            val response = executeRequest(request)
            RssInjectedPageFetchOutcome(
                result = RssInjectedPageResult(
                    statusCode = response.statusCode,
                    mimeType = response.mimeType,
                    charset = response.charset,
                    contentType = response.contentType,
                    bodyText = RssHtmlHeadInjector.insertAfterHeadOpenTag(
                        html = response.bodyText,
                        snippet = snippet
                    ),
                    setCookies = response.setCookies
                ),
                failureType = null
            )
        } catch (throwable: Throwable) {
            RssInjectedPageFetchOutcome(
                result = null,
                failureType = throwable.javaClass.simpleName
                    .takeUnless(String::isBlank)
                    ?: throwable.javaClass.name.substringAfterLast('.')
            )
        }
    }
}
