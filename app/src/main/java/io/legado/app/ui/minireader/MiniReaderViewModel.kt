package io.legado.app.ui.minireader

import io.legado.app.ui.minireader.paging.MiniPaginationConfig
import io.legado.app.ui.minireader.paging.MiniPaginationEngine
import io.legado.app.ui.minireader.paging.MiniPaginationSnapshot
import java.io.FileNotFoundException

class MiniReaderViewModel(
    private val textLoader: (String) -> MiniTextBookPayload,
    private val progressManager: ProgressStore,
    private val paginationEngine: MiniPaginationEngine,
    private val paginationConfigProvider: (MiniReaderProgressManager.MiniReaderSettings) -> MiniPaginationConfig = {
        MiniPaginationConfig(
            pageCharCapacity = (it.fontSizeSp * 2).coerceAtLeast(1),
            lineSpacingMultiplier = it.lineSpacingMultiplier
        )
    }
) {

    interface ProgressStore {
        fun loadProgress(bookUrl: String): MiniReaderProgressManager.MiniReaderProgress
        fun saveProgress(bookUrl: String, chapterIndex: Int, globalOffset: Int, force: Boolean = false): Boolean
        fun loadSettings(): MiniReaderProgressManager.MiniReaderSettings
        fun saveSettings(settings: MiniReaderProgressManager.MiniReaderSettings)
    }

    sealed interface MiniReaderState {
        data object Idle : MiniReaderState
        data object Loading : MiniReaderState
        data class Ready(
            val bookUrl: String,
            val payload: MiniTextBookPayload,
            val snapshot: MiniPaginationSnapshot,
            val settings: MiniReaderProgressManager.MiniReaderSettings
        ) : MiniReaderState

        data class Turning(
            val from: Ready,
            val target: Ready
        ) : MiniReaderState

        data class Unavailable(
            val bookUrl: String,
            val canRebind: Boolean = true
        ) : MiniReaderState

        data class EncodingUnsupported(
            val bookUrl: String,
            val message: String
        ) : MiniReaderState
    }

    var state: MiniReaderState = MiniReaderState.Idle
        private set

    fun openBook(bookUrl: String) {
        state = MiniReaderState.Loading
        try {
            val payload = textLoader(bookUrl)
            val settings = progressManager.loadSettings()
            val config = paginationConfigProvider(settings)
            val progress = progressManager.loadProgress(bookUrl)
            val paged = paginationEngine.paginate(payload.chapters, config)
            val restored = paginationEngine.reflowByGlobalOffset(progress.globalOffset, config)
            state = MiniReaderState.Ready(
                bookUrl = bookUrl,
                payload = payload,
                snapshot = if (progress.globalOffset > 0) restored else paged,
                settings = settings
            )
        } catch (e: FileNotFoundException) {
            state = MiniReaderState.Unavailable(bookUrl = bookUrl, canRebind = true)
        } catch (e: Exception) {
            state = MiniReaderState.EncodingUnsupported(
                bookUrl = bookUrl,
                message = e.localizedMessage ?: "load error"
            )
        }
    }

    fun onTurnNext() {
        val current = state
        if (current is MiniReaderState.Turning) {
            return
        }
        if (current !is MiniReaderState.Ready) {
            return
        }
        val nextSnapshot = paginationEngine.next(current.snapshot)
        if (nextSnapshot.current.index == current.snapshot.current.index) {
            return
        }
        state = MiniReaderState.Turning(
            from = current,
            target = current.copy(snapshot = nextSnapshot)
        )
    }

    fun onTurnPrev() {
        val current = state
        if (current is MiniReaderState.Turning) {
            return
        }
        if (current !is MiniReaderState.Ready) {
            return
        }
        val prevSnapshot = paginationEngine.prev(current.snapshot)
        if (prevSnapshot.current.index == current.snapshot.current.index) {
            return
        }
        state = MiniReaderState.Turning(
            from = current,
            target = current.copy(snapshot = prevSnapshot)
        )
    }

    fun onAnimationFinished() {
        val current = state
        if (current is MiniReaderState.Turning) {
            val ready = current.target
            state = ready
            progressManager.saveProgress(
                bookUrl = ready.bookUrl,
                chapterIndex = ready.snapshot.current.chapterIndex,
                globalOffset = ready.snapshot.current.startOffset
            )
        }
    }

    fun onSettingsChanged(settings: MiniReaderProgressManager.MiniReaderSettings) {
        val current = state
        progressManager.saveSettings(settings)
        if (current is MiniReaderState.Ready) {
            val config = paginationConfigProvider(settings)
            val reflow = paginationEngine.reflowByGlobalOffset(current.snapshot.current.startOffset, config)
            state = current.copy(snapshot = reflow, settings = settings)
        }
    }

    fun onJumpToChapter(chapterIndex: Int) {
        val current = state
        if (current !is MiniReaderState.Ready) {
            return
        }
        val target = current.snapshot.pages.firstOrNull { it.chapterIndex == chapterIndex } ?: return
        val config = paginationConfigProvider(current.settings)
        val reflow = paginationEngine.reflowByGlobalOffset(target.startOffset, config)
        state = current.copy(snapshot = reflow)
    }

    fun onJumpToProgress(percent: Int) {
        val current = state
        if (current !is MiniReaderState.Ready) {
            return
        }
        val boundedPercent = percent.coerceIn(0, 100)
        val fullTextLength = current.payload.fullText.length.coerceAtLeast(1)
        val targetOffset = (fullTextLength * (boundedPercent / 100f)).toInt()
        val config = paginationConfigProvider(current.settings)
        val reflow = paginationEngine.reflowByGlobalOffset(targetOffset, config)
        state = current.copy(snapshot = reflow)
    }
}
