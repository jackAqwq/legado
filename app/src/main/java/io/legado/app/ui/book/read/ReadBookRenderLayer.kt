package io.legado.app.ui.book.read

import android.os.Handler
import io.legado.app.help.config.AppConfig
import io.legado.app.model.ReadBook
import io.legado.app.service.BaseReadAloudService
import io.legado.app.ui.book.read.config.AutoReadDialog
import io.legado.app.ui.book.read.config.ReadAloudDialog
import io.legado.app.utils.dismissDialogFragment
import io.legado.app.utils.showDialogFragment
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

internal class ReadBookRenderLayer(
    private val activity: ReadBookActivity,
    private val menuLayer: ReadBookMenuLayer
) {

    fun upMenuView(handler: Handler) {
        handler.post {
            menuLayer.upMenu()
            activity.binding.readMenu.upBookView()
        }
    }

    fun upContent(
        relativePosition: Int,
        resetPageOffset: Boolean,
        success: (() -> Unit)?,
        onSeekBarProgress: () -> Unit
    ) {
        activity.lifecycleScope.launch {
            activity.binding.readView.upContent(relativePosition, resetPageOffset)
            if (relativePosition == 0) {
                onSeekBarProgress()
            }
            success?.invoke()
        }
    }

    suspend fun upContentAwait(
        relativePosition: Int,
        resetPageOffset: Boolean,
        success: (() -> Unit)?,
        onSeekBarProgress: () -> Unit
    ) {
        withContext(Main.immediate) {
            activity.binding.readView.upContent(relativePosition, resetPageOffset)
            if (relativePosition == 0) {
                onSeekBarProgress()
            }
            success?.invoke()
        }
    }

    fun upPageAnim(upRecorder: Boolean) {
        activity.lifecycleScope.launch {
            activity.binding.readView.upPageAnim(upRecorder)
        }
    }

    fun upSeekBarProgress() {
        val progress = when (AppConfig.progressBarBehavior) {
            "page" -> ReadBook.durPageIndex
            else -> ReadBook.durChapterIndex
        }
        activity.binding.readMenu.setSeekPage(progress)
    }

    fun showMenuBar() {
        activity.binding.readMenu.runMenuIn()
    }

    fun showActionMenu(isAutoPage: Boolean, isShowingSearchResult: Boolean) {
        when {
            BaseReadAloudService.isRun -> activity.showDialogFragment<ReadAloudDialog>()
            isAutoPage -> activity.showDialogFragment<AutoReadDialog>()
            isShowingSearchResult -> activity.binding.searchMenu.runMenuIn()
            else -> activity.binding.readMenu.runMenuIn()
        }
    }

    fun autoPage() {
        activity.binding.readView.autoPager.start()
        activity.binding.readMenu.setAutoPage(true)
    }

    fun autoPageStop() {
        activity.binding.readView.autoPager.stop()
        activity.binding.readMenu.setAutoPage(false)
        activity.dismissDialogFragment<AutoReadDialog>()
    }
}
