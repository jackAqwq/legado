package io.legado.app.ui.book.read

import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.data.appDb
import io.legado.app.data.entities.Book
import io.legado.app.help.AppWebDav
import io.legado.app.help.book.BookHelp
import io.legado.app.help.book.ContentProcessor
import io.legado.app.help.book.isEpub
import io.legado.app.help.book.isLocal
import io.legado.app.help.book.isLocalTxt
import io.legado.app.help.book.isMobi
import io.legado.app.lib.dialogs.selector
import io.legado.app.model.ReadBook
import io.legado.app.model.localBook.EpubFile
import io.legado.app.model.localBook.MobiFile
import io.legado.app.ui.about.AppLogDialog
import io.legado.app.ui.book.changesource.ChangeBookSourceDialog
import io.legado.app.ui.book.changesource.ChangeChapterSourceDialog
import io.legado.app.ui.book.toc.rule.TxtTocRuleDialog
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.toastOnUi
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ReadBookMenuLayer(
    private val activity: ReadBookActivity
) {

    private enum class RefreshMode {
        DUR,
        AFTER,
        ALL
    }

    private var menu: Menu? = null
    private val menuActions: Map<Int, (MenuItem) -> Unit> by lazy {
        mapOf(
            R.id.menu_change_source to { showChangeSource() },
            R.id.menu_book_change_source to { showChangeSource() },
            R.id.menu_chapter_change_source to { showChapterChangeSource() },
            R.id.menu_refresh to { refreshContent(RefreshMode.DUR) },
            R.id.menu_refresh_dur to { refreshContent(RefreshMode.DUR) },
            R.id.menu_refresh_after to { refreshContent(RefreshMode.AFTER) },
            R.id.menu_refresh_all to { refreshContent(RefreshMode.ALL) },
            R.id.menu_download to { activity.showDownloadDialog() },
            R.id.menu_add_bookmark to { activity.addBookmark() },
            R.id.menu_simulated_reading to { activity.showSimulatedReading() },
            R.id.menu_edit_content to { activity.showDialogFragment(ContentEditDialog()) },
            R.id.menu_update_toc to { handleUpdateToc() },
            R.id.menu_enable_replace to { activity.changeReplaceRuleState() },
            R.id.menu_re_segment to { item -> toggleReSegment(item) },
            R.id.menu_del_ruby_tag to { item -> toggleDelTag(item, Book.rubyTag) },
            R.id.menu_del_h_tag to { item -> toggleDelTag(item, Book.hTag) },
            R.id.menu_page_anim to { handlePageAnimConfig() },
            R.id.menu_log to { activity.showDialogFragment<AppLogDialog>() },
            R.id.menu_toc_regex to { activity.showDialogFragment(TxtTocRuleDialog(ReadBook.book?.tocUrl)) },
            R.id.menu_reverse_content to { ReadBook.book?.let { activity.viewModel.reverseContent(it) } },
            R.id.menu_set_charset to { activity.showCharsetConfig() },
            R.id.menu_image_style to { handleImageStyleConfig() },
            R.id.menu_get_progress to {
                ReadBook.book?.let {
                    activity.viewModel.syncBookProgress(it) { progress ->
                        activity.sureSyncProgress(progress)
                    }
                }
            },
            R.id.menu_cover_progress to {
                ReadBook.book?.let {
                    ReadBook.uploadProgress(true) { activity.toastOnUi(R.string.upload_book_success) }
                }
            },
            R.id.menu_same_title_removed to { handleSameTitleRemoved() },
            R.id.menu_effective_replaces to { activity.showDialogFragment<EffectiveReplacesDialog>() },
            R.id.menu_help to { activity.showHelp() }
        )
    }

    fun onPrepareOptionsMenu(menu: Menu) {
        this.menu = menu
        upMenu()
    }

    fun upMenu() {
        val menu = menu ?: return
        val book = ReadBook.book ?: return
        val onLine = !book.isLocal
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            when (item.groupId) {
                R.id.menu_group_on_line -> item.isVisible = onLine
                R.id.menu_group_local -> item.isVisible = !onLine
                R.id.menu_group_text -> item.isVisible = book.isLocalTxt
                R.id.menu_group_epub -> item.isVisible = book.isEpub
                else -> when (item.itemId) {
                    R.id.menu_enable_replace -> item.isChecked = book.getUseReplaceRule()
                    R.id.menu_re_segment -> item.isChecked = book.getReSegment()
                    R.id.menu_reverse_content -> item.isVisible = onLine
                    R.id.menu_del_ruby_tag -> item.isChecked = book.getDelTag(Book.rubyTag)
                    R.id.menu_del_h_tag -> item.isChecked = book.getDelTag(Book.hTag)
                }
            }
        }
        activity.lifecycleScope.launch {
            val show = ReadBook.inBookshelf && withContext(IO) {
                AppWebDav.isOk
            }
            menu.findItem(R.id.menu_get_progress)?.isVisible = show
            menu.findItem(R.id.menu_cover_progress)?.isVisible = show
        }
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean {
        val action = menuActions[item.itemId] ?: return false
        action(item)
        return true
    }

    private fun showChangeSource() {
        activity.binding.readMenu.runMenuOut()
        ReadBook.book?.let {
            activity.showDialogFragment(ChangeBookSourceDialog(it.name, it.author))
        }
    }

    private fun showChapterChangeSource() {
        activity.lifecycleScope.launch {
            val book = ReadBook.book ?: return@launch
            val chapter = appDb.bookChapterDao.getChapter(book.bookUrl, ReadBook.durChapterIndex)
                ?: return@launch
            activity.binding.readMenu.runMenuOut()
            activity.showDialogFragment(
                ChangeChapterSourceDialog(book.name, book.author, chapter.index, chapter.title)
            )
        }
    }

    private fun refreshContent(mode: RefreshMode) {
        if (ReadBook.bookSource == null) {
            activity.upContent()
            return
        }
        ReadBook.book?.let { book ->
            when (mode) {
                RefreshMode.DUR -> {
                    ReadBook.curTextChapter = null
                    activity.binding.readView.upContent()
                    activity.viewModel.refreshContentDur(book)
                }

                RefreshMode.AFTER -> {
                    ReadBook.clearTextChapter()
                    activity.binding.readView.upContent()
                    activity.viewModel.refreshContentAfter(book)
                }

                RefreshMode.ALL -> refreshContentAll(book)
            }
        }
    }

    private fun handleUpdateToc() {
        ReadBook.book?.let {
            if (it.isEpub) {
                BookHelp.clearCache(it)
                EpubFile.clear()
            }
            if (it.isMobi) {
                MobiFile.clear()
            }
            activity.loadChapterList(it)
        }
    }

    private fun toggleReSegment(item: MenuItem) {
        ReadBook.book?.let {
            it.setReSegment(!it.getReSegment())
            item.isChecked = it.getReSegment()
            ReadBook.loadContent(false)
        }
    }

    private fun toggleDelTag(item: MenuItem, tag: Long) {
        ReadBook.book?.let {
            item.isChecked = !item.isChecked
            if (item.isChecked) {
                it.addDelTag(tag)
            } else {
                it.removeDelTag(tag)
            }
            refreshContentAll(it)
        }
    }

    private fun handlePageAnimConfig() {
        activity.showPageAnimConfig {
            activity.binding.readView.upPageAnim()
            ReadBook.loadContent(false)
        }
    }

    private fun handleImageStyleConfig() {
        val imgStyles = arrayListOf(
            Book.imgStyleDefault,
            Book.imgStyleFull,
            Book.imgStyleText,
            Book.imgStyleSingle
        )
        activity.selector(
            R.string.image_style,
            imgStyles
        ) { _, index ->
            val imageStyle = imgStyles[index]
            ReadBook.book?.setImageStyle(imageStyle)
            if (imageStyle == Book.imgStyleSingle) {
                ReadBook.book?.setPageAnim(0)
                activity.binding.readView.upPageAnim()
            }
            ReadBook.loadContent(false)
        }
    }

    private fun handleSameTitleRemoved() {
        ReadBook.book?.let {
            val contentProcessor = ContentProcessor.get(it)
            val textChapter = ReadBook.curTextChapter
            if (textChapter != null
                && !textChapter.sameTitleRemoved
                && !contentProcessor.removeSameTitleCache.contains(
                    textChapter.chapter.getFileName("nr")
                )
            ) {
                activity.toastOnUi("未找到可移除的重复标题")
            }
        }
        activity.viewModel.reverseRemoveSameTitle()
    }

    private fun refreshContentAll(book: Book) {
        ReadBook.clearTextChapter()
        activity.binding.readView.upContent()
        activity.viewModel.refreshContentAll(book)
    }
}
