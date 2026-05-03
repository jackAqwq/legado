package io.legado.app.ui.minireader

import io.legado.app.ui.minireader.chapter.MiniChapter
import java.nio.charset.Charset

data class MiniTextBookPayload(
    val fullText: String,
    val chapters: List<MiniChapter>,
    val charset: Charset
)
