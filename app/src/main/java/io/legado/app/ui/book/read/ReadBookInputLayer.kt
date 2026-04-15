package io.legado.app.ui.book.read

import android.annotation.SuppressLint
import android.content.Intent
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.help.TTS
import io.legado.app.help.config.AppConfig
import io.legado.app.help.config.ReadBookConfig
import io.legado.app.model.ReadBook
import io.legado.app.service.BaseReadAloudService
import io.legado.app.ui.book.bookmark.BookmarkDialog
import io.legado.app.ui.book.read.page.delegate.ScrollPageDelegate
import io.legado.app.ui.book.read.page.entities.PageDirection
import io.legado.app.ui.dict.DictDialog
import io.legado.app.ui.replace.edit.ReplaceEditActivity
import io.legado.app.utils.Debounce
import io.legado.app.utils.LogUtils
import io.legado.app.utils.navigationBarGravity
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.toastOnUi
import io.legado.app.utils.invisible
import io.legado.app.utils.visible
import kotlinx.coroutines.launch

internal class ReadBookInputLayer(
    private val activity: ReadBookActivity,
    private val replaceActivity: ActivityResultLauncher<Intent>
) {

    val textActionMenu: TextActionMenu by lazy {
        TextActionMenu(activity, activity)
    }

    private var tts: TTS? = null
    private val nextPageDebounce by lazy { Debounce { keyPage(PageDirection.NEXT) } }
    private val prevPageDebounce by lazy { Debounce { keyPage(PageDirection.PREV) } }

    fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action
        val isDown = action == 0
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (isDown && !activity.binding.readMenu.canShowMenu) {
                activity.binding.readMenu.runMenuIn()
                return true
            }
            if (!isDown && !activity.binding.readMenu.canShowMenu) {
                activity.binding.readMenu.canShowMenu = true
                return true
            }
        }
        return false
    }

    fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (0 != (event.source and InputDevice.SOURCE_CLASS_POINTER)) {
            if (event.action == MotionEvent.ACTION_SCROLL) {
                val axisValue = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                LogUtils.d("onGenericMotionEvent", "axisValue = $axisValue")
                if (axisValue < 0.0f) {
                    mouseWheelPage(PageDirection.NEXT, axisValue)
                } else {
                    mouseWheelPage(PageDirection.PREV, axisValue)
                }
                return true
            }
        }
        return false
    }

    fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (activity.isMenuLayoutVisibleForInput()) {
            return false
        }
        val longPress = event.repeatCount > 0
        when {
            activity.isPrevKey(keyCode) -> {
                handleKeyPage(PageDirection.PREV, longPress)
                return true
            }

            activity.isNextKey(keyCode) -> {
                handleKeyPage(PageDirection.NEXT, longPress)
                return true
            }
        }
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> if (volumeKeyPage(PageDirection.PREV, longPress)) {
                return true
            }

            KeyEvent.KEYCODE_VOLUME_DOWN -> if (volumeKeyPage(PageDirection.NEXT, longPress)) {
                return true
            }

            KeyEvent.KEYCODE_PAGE_UP -> {
                handleKeyPage(PageDirection.PREV, longPress)
                return true
            }

            KeyEvent.KEYCODE_PAGE_DOWN -> {
                handleKeyPage(PageDirection.NEXT, longPress)
                return true
            }

            KeyEvent.KEYCODE_SPACE -> {
                handleKeyPage(PageDirection.NEXT, longPress)
                return true
            }
        }
        return false
    }

    fun onKeyUp(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (volumeKeyPage(PageDirection.NONE, false)) {
                    return true
                }
            }
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!activity.binding.readView.isTextSelected) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> textActionMenu.dismiss()
            MotionEvent.ACTION_MOVE -> {
                when (v.id) {
                    R.id.cursor_left -> if (!activity.binding.readView.curPage.getReverseStartCursor()) {
                        activity.binding.readView.curPage.selectStartMove(
                            event.rawX + activity.binding.cursorLeft.width,
                            event.rawY - activity.binding.cursorLeft.height
                        )
                    } else {
                        activity.binding.readView.curPage.selectEndMove(
                            event.rawX - activity.binding.cursorRight.width,
                            event.rawY - activity.binding.cursorRight.height
                        )
                    }

                    R.id.cursor_right -> if (activity.binding.readView.curPage.getReverseEndCursor()) {
                        activity.binding.readView.curPage.selectStartMove(
                            event.rawX + activity.binding.cursorLeft.width,
                            event.rawY - activity.binding.cursorLeft.height
                        )
                    } else {
                        activity.binding.readView.curPage.selectEndMove(
                            event.rawX - activity.binding.cursorRight.width,
                            event.rawY - activity.binding.cursorRight.height
                        )
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                activity.binding.readView.curPage.resetReverseCursor()
                showTextActionMenu()
            }
        }
        return true
    }

    fun upSelectedStart(x: Float, y: Float, top: Float) {
        activity.binding.cursorLeft.x = x - activity.binding.cursorLeft.width
        activity.binding.cursorLeft.y = y
        activity.binding.cursorLeft.visible(true)
        activity.binding.textMenuPosition.x = x
        activity.binding.textMenuPosition.y = top
    }

    fun upSelectedEnd(x: Float, y: Float) {
        activity.binding.cursorRight.x = x
        activity.binding.cursorRight.y = y
        activity.binding.cursorRight.visible(true)
    }

    fun onCancelSelect() {
        activity.binding.cursorLeft.invisible()
        activity.binding.cursorRight.invisible()
        textActionMenu.dismiss()
    }

    fun onLongScreenshotTouchEvent(event: MotionEvent): Boolean {
        return activity.binding.readView.onTouchEvent(event)
    }

    fun showTextActionMenu() {
        val navigationBarHeight =
            if (!ReadBookConfig.hideNavigationBar && navigationBarGravity == Gravity.BOTTOM)
                activity.binding.navigationBar.height else 0
        textActionMenu.show(
            activity.binding.textMenuPosition,
            activity.binding.root.height + navigationBarHeight,
            activity.binding.textMenuPosition.x.toInt(),
            activity.binding.textMenuPosition.y.toInt(),
            activity.binding.cursorLeft.y.toInt() + activity.binding.cursorLeft.height,
            activity.binding.cursorRight.x.toInt(),
            activity.binding.cursorRight.y.toInt() + activity.binding.cursorRight.height
        )
    }

    fun selectedText(): String = activity.binding.readView.getSelectText()

    fun onMenuItemSelected(itemId: Int): Boolean {
        when (itemId) {
            R.id.menu_aloud -> when (AppConfig.contentSelectSpeakMod) {
                1 -> activity.lifecycleScope.launch {
                    activity.binding.readView.aloudStartSelect()
                }

                else -> speak(selectedText())
            }

            R.id.menu_bookmark -> activity.binding.readView.curPage.let {
                val bookmark = it.createBookmark()
                if (bookmark == null) {
                    activity.toastOnUi(R.string.create_bookmark_error)
                } else {
                    activity.showDialogFragment(BookmarkDialog(bookmark))
                }
                return true
            }

            R.id.menu_replace -> {
                val scopes = arrayListOf<String>()
                ReadBook.book?.name?.let {
                    scopes.add(it)
                }
                ReadBook.bookSource?.bookSourceUrl?.let {
                    scopes.add(it)
                }
                val text = selectedText().lineSequence().map { it.trim() }.joinToString("\n")
                replaceActivity.launch(
                    ReplaceEditActivity.startIntent(
                        activity,
                        pattern = text,
                        scope = scopes.joinToString(";")
                    )
                )
                return true
            }

            R.id.menu_search_content -> {
                activity.viewModel.searchContentQuery = selectedText()
                activity.openSearchActivity(selectedText())
                return true
            }

            R.id.menu_dict -> {
                activity.showDialogFragment(DictDialog(selectedText()))
                return true
            }
        }
        return false
    }

    fun onMenuActionFinally() {
        textActionMenu.dismiss()
        activity.binding.readView.cancelSelect()
    }

    fun onDestroy() {
        tts?.clearTts()
        textActionMenu.dismiss()
    }

    private fun speak(text: String) {
        if (tts == null) {
            tts = TTS()
        }
        tts?.speak(text)
    }

    private fun mouseWheelPage(direction: PageDirection, distance: Float) {
        if (activity.isMenuLayoutVisibleForInput() || !AppConfig.mouseWheelPage) {
            return
        }
        if (activity.binding.readView.isScroll) {
            (activity.binding.readView.pageDelegate as? ScrollPageDelegate)
                ?.curPage
                ?.scroll((distance * 50).toInt())
        } else {
            keyPageDebounce(direction, mouseWheel = true, longPress = false)
        }
    }

    private fun volumeKeyPage(direction: PageDirection, longPress: Boolean): Boolean {
        if (!AppConfig.volumeKeyPage) {
            return false
        }
        if (!AppConfig.volumeKeyPageOnPlay && BaseReadAloudService.isPlay()) {
            return false
        }
        handleKeyPage(direction, longPress)
        return true
    }

    private fun handleKeyPage(direction: PageDirection, longPress: Boolean) {
        if (AppConfig.keyPageOnLongPress || direction == PageDirection.NONE) {
            keyPage(direction)
        } else {
            keyPageDebounce(direction, longPress = longPress)
        }
    }

    private fun keyPageDebounce(
        direction: PageDirection,
        mouseWheel: Boolean = false,
        longPress: Boolean
    ) {
        if (longPress) {
            return
        }
        nextPageDebounce.apply {
            wait = if (mouseWheel) 200L else 600L
            leading = !mouseWheel
            trailing = mouseWheel
        }
        prevPageDebounce.apply {
            wait = if (mouseWheel) 200L else 600L
            leading = !mouseWheel
            trailing = mouseWheel
        }
        when (direction) {
            PageDirection.NEXT -> nextPageDebounce.invoke()
            PageDirection.PREV -> prevPageDebounce.invoke()
            else -> {}
        }
    }

    private fun keyPage(direction: PageDirection) {
        activity.binding.readView.cancelSelect()
        activity.binding.readView.pageDelegate?.isCancel = false
        activity.binding.readView.pageDelegate?.keyTurnPage(direction)
    }
}
