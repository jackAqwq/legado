package io.legado.app.ui.book.read.page.provider

import org.junit.Assert.assertEquals
import org.junit.Test

class TextClusterSplitterTest {

    @Test
    fun merge_zero_width_measurement_run_for_non_zero_width_chars() {
        val text = "ab"
        val widthsArray = floatArrayOf(5f, 0f)

        val (parts, widths) = TextClusterSplitter.measureTextSplit(text, widthsArray)

        assertEquals(listOf("ab"), parts)
        assertEquals(listOf(5f), widths)
    }

    @Test
    fun keep_zero_width_unicode_char_as_own_cluster() {
        val text = "a\u200Bb"
        val widthsArray = floatArrayOf(5f, 0f, 5f)

        val (parts, widths) = TextClusterSplitter.measureTextSplit(text, widthsArray)

        assertEquals(listOf("a", "\u200B", "b"), parts)
        assertEquals(listOf(5f, 0f, 5f), widths)
    }

    @Test
    fun support_non_zero_start_offset() {
        val text = "xy"
        val widthsArray = floatArrayOf(9f, 8f, 4f, 0f)

        val (parts, widths) = TextClusterSplitter.measureTextSplit(
            text = text,
            widthsArray = widthsArray,
            start = 2
        )

        assertEquals(listOf("xy"), parts)
        assertEquals(listOf(4f), widths)
    }
}
