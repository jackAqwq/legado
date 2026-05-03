package io.legado.app.ui.minireader

import io.legado.app.ui.minireader.chapter.MiniChapter
import io.legado.app.ui.minireader.paging.MiniPaginationConfig
import io.legado.app.ui.minireader.paging.MiniPaginationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.FileNotFoundException
import java.nio.charset.Charset

class MiniReaderViewModelTest {

    @Test
    fun open_book_should_emit_ready_state_with_snapshot() {
        val book = MiniTextBookPayload(
            fullText = "第一章\n这是正文内容",
            chapters = listOf(MiniChapter("第一章", "这是正文内容", 0, 6)),
            charset = Charsets.UTF_8
        )
        val vm = MiniReaderViewModel(
            textLoader = { book },
            progressManager = FakeProgressManager(),
            paginationEngine = MiniPaginationEngine()
        )

        vm.openBook("content://book/a.txt")

        val state = vm.state
        assertTrue(state is MiniReaderViewModel.MiniReaderState.Ready)
        val ready = state as MiniReaderViewModel.MiniReaderState.Ready
        assertEquals("content://book/a.txt", ready.bookUrl)
        assertNotNull(ready.snapshot.current)
        assertEquals("第一章", ready.snapshot.current.title)
    }

    @Test
    fun lost_uri_should_emit_unavailable_and_rebind_action() {
        val vm = MiniReaderViewModel(
            textLoader = {
                throw FileNotFoundException("missing")
            },
            progressManager = FakeProgressManager(),
            paginationEngine = MiniPaginationEngine()
        )

        vm.openBook("content://book/missing.txt")

        val state = vm.state
        assertTrue(state is MiniReaderViewModel.MiniReaderState.Unavailable)
        val unavailable = state as MiniReaderViewModel.MiniReaderState.Unavailable
        assertEquals("content://book/missing.txt", unavailable.bookUrl)
        assertTrue(unavailable.canRebind)
    }

    @Test
    fun animation_lock_should_ignore_reentrant_page_turns() {
        val book = MiniTextBookPayload(
            fullText = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ",
            chapters = listOf(MiniChapter("正文", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", 0, 36)),
            charset = Charsets.UTF_8
        )
        val vm = MiniReaderViewModel(
            textLoader = { book },
            progressManager = FakeProgressManager(),
            paginationEngine = MiniPaginationEngine(),
            paginationConfigProvider = {
                MiniPaginationConfig(pageCharCapacity = 8, lineSpacingMultiplier = 1f)
            }
        )
        vm.openBook("content://book/long.txt")

        vm.onTurnNext()
        val turning = vm.state
        assertTrue(turning is MiniReaderViewModel.MiniReaderState.Turning)

        vm.onTurnNext()
        val stillTurning = vm.state
        assertTrue(stillTurning is MiniReaderViewModel.MiniReaderState.Turning)

        vm.onAnimationFinished()
        val ready = vm.state as MiniReaderViewModel.MiniReaderState.Ready
        assertEquals(1, ready.snapshot.current.index)
    }
}

private class FakeProgressManager : MiniReaderViewModel.ProgressStore {
    private var progress = MiniReaderProgressManager.MiniReaderProgress(0, 0)
    private var settings = MiniReaderProgressManager.MiniReaderSettings(
        fontSizeSp = 18,
        lineSpacingMultiplier = 1.35f,
        bgMode = MiniReaderProgressManager.BG_MODE_LIGHT,
        brightness = 100
    )

    override fun loadProgress(bookUrl: String): MiniReaderProgressManager.MiniReaderProgress {
        return progress
    }

    override fun saveProgress(bookUrl: String, chapterIndex: Int, globalOffset: Int, force: Boolean): Boolean {
        progress = MiniReaderProgressManager.MiniReaderProgress(chapterIndex, globalOffset)
        return true
    }

    override fun loadSettings(): MiniReaderProgressManager.MiniReaderSettings {
        return settings
    }

    override fun saveSettings(settings: MiniReaderProgressManager.MiniReaderSettings) {
        this.settings = settings
    }
}
