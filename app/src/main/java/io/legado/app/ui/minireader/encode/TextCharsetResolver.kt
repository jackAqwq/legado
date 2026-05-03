package io.legado.app.ui.minireader.encode

import io.legado.app.utils.Utf8BomUtils
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset

object TextCharsetResolver {

    private val gbk: Charset = Charset.forName("GBK")

    fun decode(bytes: ByteArray): Pair<String, Charset> {
        val normalizedBytes = Utf8BomUtils.removeUTF8BOM(bytes)
        return try {
            normalizedBytes.decodeToString(throwOnInvalidSequence = true) to Charsets.UTF_8
        } catch (_: CharacterCodingException) {
            String(normalizedBytes, gbk) to gbk
        }
    }
}
