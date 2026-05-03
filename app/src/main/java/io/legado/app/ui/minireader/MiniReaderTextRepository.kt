package io.legado.app.ui.minireader

import android.content.Context
import android.net.Uri
import io.legado.app.ui.minireader.chapter.ChapterSplitter
import io.legado.app.ui.minireader.encode.TextCharsetResolver
import io.legado.app.utils.inputStream

class MiniReaderTextRepository(
    private val context: Context
) {

    fun load(uri: Uri): MiniTextBookPayload {
        val bytes = uri.inputStream(context).getOrThrow().use { input ->
            input.readBytes()
        }
        val (decodedText, usedCharset) = TextCharsetResolver.decode(bytes)
        val chapters = ChapterSplitter.split(decodedText)
        return MiniTextBookPayload(
            fullText = decodedText,
            chapters = chapters,
            charset = usedCharset
        )
    }
}
