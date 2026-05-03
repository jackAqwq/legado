package io.legado.app.ui.minireader

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.legado.app.ui.file.HandleFileContract
import io.legado.app.ui.minireader.paging.MiniPaginationEngine
import io.legado.app.ui.minireader.ui.MiniReaderScreen
import io.legado.app.utils.toastOnUi

class MiniReaderActivity : ComponentActivity() {

    private val stateStore: MutableState<MiniReaderViewModel.MiniReaderState> =
        mutableStateOf(MiniReaderViewModel.MiniReaderState.Idle)
    private var currentBookUrl: String? = null

    private val progressStore by lazy {
        val manager = MiniReaderProgressManager(context = this)
        object : MiniReaderViewModel.ProgressStore {
            override fun loadProgress(bookUrl: String): MiniReaderProgressManager.MiniReaderProgress {
                return manager.loadProgress(bookUrl)
            }

            override fun saveProgress(
                bookUrl: String,
                chapterIndex: Int,
                globalOffset: Int,
                force: Boolean
            ): Boolean {
                return manager.saveProgress(bookUrl, chapterIndex, globalOffset, force)
            }

            override fun loadSettings(): MiniReaderProgressManager.MiniReaderSettings {
                return manager.loadSettings()
            }

            override fun saveSettings(settings: MiniReaderProgressManager.MiniReaderSettings) {
                manager.saveSettings(settings)
            }
        }
    }

    private val miniReaderViewModel by lazy {
        MiniReaderViewModel(
            textLoader = { bookUrl ->
                MiniReaderTextRepository(this).load(Uri.parse(bookUrl))
            },
            progressManager = progressStore,
            paginationEngine = MiniPaginationEngine()
        )
    }

    private val miniReaderImportResult = registerForActivityResult(HandleFileContract()) { result ->
        val uri = result.uri ?: return@registerForActivityResult
        runCatching {
            MiniReaderBookshelfRepository().importFromPickedUri(uri)
        }.onSuccess { book ->
            openBook(book.bookUrl)
        }.onFailure {
            toastOnUi(it.localizedMessage ?: "Import failed")
            val url = currentBookUrl
            if (url.isNullOrBlank()) {
                finish()
            }
        }
    }

    private val filePickerGateway by lazy {
        MiniReaderFilePickerGateway(miniReaderImportResult)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val launchBookUrl = intent?.getStringExtra(MiniReaderContract.EXTRA_BOOK_URL)
        val openPicker = intent?.getBooleanExtra(MiniReaderContract.EXTRA_OPEN_PICKER, false) == true
        if (launchBookUrl.isNullOrBlank() && !openPicker) {
            finish()
            return
        }

        setContent {
            MiniReaderScreen(
                state = stateStore.value,
                onTurnPrev = {
                    miniReaderViewModel.onTurnPrev()
                    syncState()
                },
                onTurnNext = {
                    miniReaderViewModel.onTurnNext()
                    syncState()
                },
                onAnimationFinished = {
                    miniReaderViewModel.onAnimationFinished()
                    syncState()
                },
                onSettingsChanged = { settings ->
                    miniReaderViewModel.onSettingsChanged(settings)
                    syncState()
                },
                onJumpToChapter = { chapterIndex ->
                    miniReaderViewModel.onJumpToChapter(chapterIndex)
                    syncState()
                },
                onJumpToProgress = { percent ->
                    miniReaderViewModel.onJumpToProgress(percent)
                    syncState()
                },
                onOpenPicker = {
                    filePickerGateway.pickTxt()
                }
            )
        }

        if (savedInstanceState == null) {
            when {
                openPicker -> filePickerGateway.pickTxt()
                !launchBookUrl.isNullOrBlank() -> openBook(launchBookUrl)
            }
        }
    }

    private fun openBook(bookUrl: String) {
        currentBookUrl = bookUrl
        miniReaderViewModel.openBook(bookUrl)
        syncState()
    }

    private fun syncState() {
        stateStore.value = miniReaderViewModel.state
    }
}
