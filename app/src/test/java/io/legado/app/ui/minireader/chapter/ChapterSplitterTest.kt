package io.legado.app.ui.minireader.chapter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChapterSplitterTest {

    @Test
    fun split_should_create_default_single_chapter_for_short_text() {
        val text = "这是一段很短的正文，没有明显章节标题。"

        val chapters = ChapterSplitter.split(text)

        assertEquals(1, chapters.size)
        assertEquals("正文", chapters.first().title)
        assertEquals(text.trim(), chapters.first().content)
    }

    @Test
    fun split_should_detect_title_like_lines_as_chapters() {
        val text = """
            序章
            这是序章内容。

            第一章 开始
            这是第一章内容。

            第2章 继续
            这是第二章内容。
        """.trimIndent()

        val chapters = ChapterSplitter.split(text)

        assertEquals(3, chapters.size)
        assertEquals("序章", chapters[0].title)
        assertEquals("第一章 开始", chapters[1].title)
        assertEquals("第2章 继续", chapters[2].title)
        assertTrue(chapters.all { it.content.isNotBlank() })
    }
}
