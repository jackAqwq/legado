package io.legado.app.ui.minireader.paging

import io.legado.app.ui.minireader.chapter.MiniChapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MiniPaginationEngineTest {

    @Test
    fun next_should_stop_at_last_page() {
        val chapter = MiniChapter(
            title = "正文",
            content = "abcdefghijk",
            startOffset = 0,
            endOffset = 11
        )
        val engine = MiniPaginationEngine()
        var snapshot = engine.paginate(
            chapters = listOf(chapter),
            config = MiniPaginationConfig(pageCharCapacity = 4, lineSpacingMultiplier = 1f)
        )

        assertEquals(0, snapshot.current.index)
        assertNull(snapshot.prev)
        assertNotNull(snapshot.next)

        repeat(10) {
            snapshot = engine.next(snapshot)
        }

        assertEquals(snapshot.pages.last().index, snapshot.current.index)
        assertNull(snapshot.next)

        snapshot = engine.prev(snapshot)
        assertEquals(snapshot.pages.last().index - 1, snapshot.current.index)
    }

    @Test
    fun reflow_should_restore_by_global_offset_not_old_page_index() {
        val text = "abcdefghijklmnopqrstuvwxyz0123456789"
        val chapter = MiniChapter(
            title = "正文",
            content = text,
            startOffset = 0,
            endOffset = text.length
        )
        val engine = MiniPaginationEngine()
        var snapshot = engine.paginate(
            chapters = listOf(chapter),
            config = MiniPaginationConfig(pageCharCapacity = 8, lineSpacingMultiplier = 1f)
        )

        snapshot = engine.next(snapshot)
        snapshot = engine.next(snapshot)
        assertEquals(16, snapshot.current.startOffset)

        val reflowed = engine.reflowByGlobalOffset(
            oldOffset = snapshot.current.startOffset,
            config = MiniPaginationConfig(pageCharCapacity = 11, lineSpacingMultiplier = 1f)
        )

        assertEquals(1, reflowed.current.index)
        assertEquals(11, reflowed.current.startOffset)
        assertTrue(reflowed.current.text.contains(text[16]))
    }

    @Test
    fun paginate_should_expose_prev_current_next_for_overlay_rendering() {
        val chapter = MiniChapter(
            title = "正文",
            content = "0123456789",
            startOffset = 100,
            endOffset = 110
        )
        val engine = MiniPaginationEngine()
        val first = engine.paginate(
            chapters = listOf(chapter),
            config = MiniPaginationConfig(pageCharCapacity = 3, lineSpacingMultiplier = 1f)
        )

        assertNull(first.prev)
        assertNotNull(first.next)
        val second = engine.next(first)
        assertNotNull(second.prev)
        assertNotNull(second.next)
        val last = engine.next(engine.next(engine.next(second)))
        assertNotNull(last.prev)
        assertNull(last.next)
    }
}
