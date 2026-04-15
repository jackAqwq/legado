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

    private var menu: Menu? = null

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
        when (item.itemId) {
            R.id.menu_change_source,
            R.id.menu_book_change_source -> {
                activity.binding.readMenu.runMenuOut()
                ReadBook.book?.let {
                    activity.showDialogFragment(ChangeBookSourceDialog(it.name, it.author))
                }
                return true
            }

            R.id.menu_chapter_change_source -> {
                activity.lifecycleScope.launch {
                    val book = ReadBook.book ?: return@launch
                    val chapter =
                        appDb.bookChapterDao.getChapter(book.bookUrl, ReadBook.durChapterIndex)
                            ?: return@launch
                    activity.binding.readMenu.runMenuOut()
                    activity.showDialogFragment(
                        ChangeChapterSourceDialog(book.name, book.author, chapter.index, chapter.title)
                    )
                }
                return true
            }

            R.id.menu_refresh,
            R.id.menu_refresh_dur -> {
                if (ReadBook.bookSource == null) {
                    activity.upContent()
                } else {
                    ReadBook.book?.let {
                        ReadBook.curTextChapter = null
                        activity.binding.readView.upContent()
                        activity.viewModel.refreshContentDur(it)
                    }
                }
                return true
            }

            R.id.menu_refresh_after -> {
                if (ReadBook.bookSource == null) {
                    activity.upContent()
                } else {
                    ReadBook.book?.let {
                        ReadBook.clearTextChapter()
                        activity.binding.readView.upContent()
                        activity.viewModel.refreshContentAfter(it)
                    }
                }
                return true
            }

            R.id.menu_refresh_all -> {
                if (ReadBook.bookSource == null) {
                    activity.upContent()
                } else {
                    ReadBook.book?.let {
                        refreshContentAll(it)
                    }
                }
                return true
            }

            R.id.menu_download -> {
                activity.showDownloadDialog()
                return true
            }

            R.id.menu_add_bookmark -> {
                activity.addBookmark()
                return true
            }

            R.id.menu_simulated_reading -> {
                activity.showSimulatedReading()
                return true
            }

            R.id.menu_edit_content -> {
                activity.showDialogFragment(ContentEditDialog())
                return true
            }

            R.id.menu_update_toc -> {
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
                return true
            }

            R.id.menu_enable_replace -> {
                activity.changeReplaceRuleState()
                return true
            }

            R.id.menu_re_segment -> {
                ReadBook.book?.let {
                    it.setReSegment(!it.getReSegment())
                    item.isChecked = it.getReSegment()
                    ReadBook.loadContent(false)
                }
                return true
            }

            R.id.menu_del_ruby_tag -> {
                ReadBook.book?.let {
                    item.isChecked = !item.isChecked
                    if (item.isChecked) {
                        it.addDelTag(Book.rubyTag)
                    } else {
                        it.removeDelTag(Book.rubyTag)
                    }
                    refreshContentAll(it)
                }
                return true
            }

            R.id.menu_del_h_tag -> {
                ReadBook.book?.let {
                    item.isChecked = !item.isChecked
                    if (item.isChecked) {
                        it.addDelTag(Book.hTag)
                    } else {
                        it.removeDelTag(Book.hTag)
                    }
                    refreshContentAll(it)
                }
                return true
            }

            R.id.menu_page_anim -> {
                activity.showPageAnimConfig {
                    activity.binding.readView.upPageAnim()
                    ReadBook.loadContent(false)
                }
                return true
            }

            R.id.menu_log -> {
                activity.showDialogFragment<AppLogDialog>()
                return true
            }

            R.id.menu_toc_regex -> {
                activity.showDialogFragment(TxtTocRuleDialog(ReadBook.book?.tocUrl))
                return true
            }

            R.id.menu_reverse_content -> {
                ReadBook.book?.let {
                    activity.viewModel.reverseContent(it)
                }
                return true
            }

            R.id.menu_set_charset -> {
                activity.showCharsetConfig()
                return true
            }

            R.id.menu_image_style -> {
                val imgStyles =
                    arrayListOf(
                        Book.imgStyleDefault, Book.imgStyleFull, Book.imgStyleText,
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
                return true
            }

            R.id.menu_get_progress -> {
                ReadBook.book?.let {
                    activity.viewModel.syncBookProgress(it) { progress ->
                        activity.sureSyncProgress(progress)
                    }
                }
                return true
            }

            R.id.menu_cover_progress -> {
                ReadBook.book?.let {
                    ReadBook.uploadProgress(true) { activity.toastOnUi(R.string.upload_book_success) }
                }
                return true
            }

            R.id.menu_same_title_removed -> {
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
                return true
            }

            R.id.menu_effective_replaces -> {
                activity.showDialogFragment<EffectiveReplacesDialog>()
                return true
            }

            R.id.menu_help -> {
                activity.showHelp()
                return true
            }
        }
        return false
    }

    private fun refreshContentAll(book: Book) {
        ReadBook.clearTextChapter()
        activity.binding.readView.upContent()
        activity.viewModel.refreshContentAll(book)
    }
}
