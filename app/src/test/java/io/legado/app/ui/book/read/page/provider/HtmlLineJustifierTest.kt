package io.legado.app.ui.book.read.page.provider

import android.graphics.Canvas
import io.legado.app.ui.book.read.page.ContentTextView
import io.legado.app.ui.book.read.page.entities.TextLine
import io.legado.app.ui.book.read.page.entities.column.TextBaseColumn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HtmlLineJustifierTest {

    @Test
    fun justify_should_return_null_for_empty_columns() {
        assertNull(HtmlLineJustifier.justify(mutableListOf(), lineWidth = 100))
    }

    @Test
    fun justify_should_keep_original_columns_when_residual_width_is_not_positive() {
        val columns = mutableListOf(
            column(0f, 20f, "A"),
            column(20f, 40f, "B")
        )

        val result = HtmlLineJustifier.justify(columns, lineWidth = 40)

        assertEquals(0f, result!!.wordSpacing)
        assertEquals(0f, columns[0].start)
        assertEquals(20f, columns[0].end)
        assertEquals(20f, columns[1].start)
        assertEquals(40f, columns[1].end)
    }

    @Test
    fun justify_should_expand_each_space_when_multiple_spaces_exist() {
        val columns = mutableListOf(
            column(0f, 10f, "A"),
            column(10f, 15f, " "),
            column(15f, 25f, "B"),
            column(25f, 30f, " "),
            column(30f, 40f, "C")
        )

        val result = HtmlLineJustifier.justify(columns, lineWidth = 60)

        assertEquals(10f, result!!.wordSpacing)
        assertEquals(10f, columns[1].start)
        assertEquals(25f, columns[1].end)
        assertEquals(25f, columns[2].start)
        assertEquals(35f, columns[2].end)
        assertEquals(35f, columns[3].start)
        assertEquals(50f, columns[3].end)
        assertEquals(50f, columns[4].start)
        assertEquals(60f, columns[4].end)
    }

    @Test
    fun justify_should_distribute_char_spacing_when_spaces_are_missing() {
        val columns = mutableListOf(
            column(0f, 10f, "A"),
            column(10f, 20f, "B"),
            column(20f, 30f, "C")
        )

        val result = HtmlLineJustifier.justify(columns, lineWidth = 50)

        assertEquals(0f, result!!.wordSpacing)
        assertEquals(0f, columns[0].start)
        assertEquals(20f, columns[0].end)
        assertEquals(20f, columns[1].start)
        assertEquals(40f, columns[1].end)
        assertEquals(40f, columns[2].start)
        assertEquals(50f, columns[2].end)
    }

    private fun column(start: Float, end: Float, char: String): FakeTextColumn {
        return FakeTextColumn(
            start = start,
            end = end,
            charData = char
        )
    }

    private data class FakeTextColumn(
        override var start: Float,
        override var end: Float,
        override val charData: String
    ) : TextBaseColumn {
        override var selected: Boolean = false
        override var isSearchResult: Boolean = false
        override var textLine: TextLine
            get() = error("not used in HtmlLineJustifierTest")
            set(_) = Unit

        override fun draw(view: ContentTextView, canvas: Canvas) = Unit
    }
}
