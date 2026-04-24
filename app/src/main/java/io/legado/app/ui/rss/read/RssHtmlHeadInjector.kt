package io.legado.app.ui.rss.read

internal object RssHtmlHeadInjector {

    fun insertAfterHeadOpenTag(html: String, snippet: String): String {
        val headIndex = html.indexOf("<head", ignoreCase = true)
        if (headIndex < 0) return html
        val closingHeadIndex = html.indexOf('>', startIndex = headIndex)
        if (closingHeadIndex < 0) return html
        return StringBuilder(html)
            .insert(closingHeadIndex + 1, snippet)
            .toString()
    }
}
