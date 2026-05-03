package io.legado.app.ui.minireader.encode

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextCharsetResolverTest {

    @Test
    fun decode_should_fallback_to_gbk_when_utf8_invalid() {
        val source = "中文章节"
        val bytes = source.toByteArray(charset("GBK"))

        val (decodedText, usedCharset) = TextCharsetResolver.decode(bytes)

        assertEquals(charset("GBK"), usedCharset)
        assertEquals(source, decodedText)
    }

    @Test
    fun decode_should_keep_utf8_when_input_is_utf8() {
        val source = "第一章 开始"
        val bytes = source.toByteArray(Charsets.UTF_8)

        val (decodedText, usedCharset) = TextCharsetResolver.decode(bytes)

        assertEquals(Charsets.UTF_8, usedCharset)
        assertEquals(source, decodedText)
    }

    @Test
    fun decode_should_strip_utf8_bom() {
        val source = "正文内容"
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + source.toByteArray(Charsets.UTF_8)

        val (decodedText, usedCharset) = TextCharsetResolver.decode(bytes)

        assertEquals(Charsets.UTF_8, usedCharset)
        assertEquals(source, decodedText)
        assertTrue(decodedText.isNotBlank())
    }
}
