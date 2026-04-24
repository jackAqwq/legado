package io.legado.app.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadBookLoadDeciderTest {

    @Test
    fun chapter_in_render_window_accepts_current_and_neighbors() {
        val durChapterIndex = 10
        assertTrue(ReadBookLoadDecider.isChapterInRenderWindow(9, durChapterIndex))
        assertTrue(ReadBookLoadDecider.isChapterInRenderWindow(10, durChapterIndex))
        assertTrue(ReadBookLoadDecider.isChapterInRenderWindow(11, durChapterIndex))
        assertFalse(ReadBookLoadDecider.isChapterInRenderWindow(8, durChapterIndex))
        assertFalse(ReadBookLoadDecider.isChapterInRenderWindow(12, durChapterIndex))
    }

    @Test
    fun scroll_update_requires_up_content_scroll_mode_and_nearby_page() {
        assertTrue(
            ReadBookLoadDecider.shouldUpdateForScroll(
                upContent = true,
                isScrollMode = true,
                layoutPageIndex = 5,
                durPageIndex = 7
            )
        )
        assertFalse(
            ReadBookLoadDecider.shouldUpdateForScroll(
                upContent = false,
                isScrollMode = true,
                layoutPageIndex = 5,
                durPageIndex = 7
            )
        )
        assertFalse(
            ReadBookLoadDecider.shouldUpdateForScroll(
                upContent = true,
                isScrollMode = false,
                layoutPageIndex = 5,
                durPageIndex = 7
            )
        )
        assertFalse(
            ReadBookLoadDecider.shouldUpdateForScroll(
                upContent = true,
                isScrollMode = true,
                layoutPageIndex = 20,
                durPageIndex = 7
            )
        )
    }

    @Test
    fun next_preview_only_renders_first_two_pages() {
        assertTrue(ReadBookLoadDecider.shouldRenderNextPreviewPage(0))
        assertTrue(ReadBookLoadDecider.shouldRenderNextPreviewPage(1))
        assertFalse(ReadBookLoadDecider.shouldRenderNextPreviewPage(2))
    }
}
