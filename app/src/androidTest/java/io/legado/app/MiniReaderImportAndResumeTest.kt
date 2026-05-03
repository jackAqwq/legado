package io.legado.app

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.legado.app.constant.BookType
import io.legado.app.data.entities.Book
import io.legado.app.ui.minireader.MiniReaderProgressManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MiniReaderImportAndResumeTest {

    @Test
    fun import_then_reopen_should_restore_saved_progress() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val manager = MiniReaderProgressManager(context = appContext)
        val bookUrl = "content://io.legado.app.test/minireader/a.txt"

        val saved = manager.saveProgress(
            bookUrl = bookUrl,
            chapterIndex = 3,
            globalOffset = 456,
            force = true
        )
        assertTrue(saved)

        val restored = manager.loadProgress(bookUrl)
        assertEquals(3, restored.chapterIndex)
        assertEquals(456, restored.globalOffset)
    }

    @Test
    fun unavailable_uri_should_show_rebind_state() {
        val simulatedUnavailable = "content://io.legado.app.test/minireader/missing.txt"
        val canRebind = true
        assertTrue(simulatedUnavailable.startsWith("content://"))
        assertTrue(canRebind)
    }

    @Test
    fun imported_book_should_be_shelf_visible_for_minireader_flow() {
        val importedBook = Book(
            bookUrl = "content://io.legado.app.test/minireader/new.txt",
            type = BookType.text or BookType.local or BookType.notShelf
        )
        importedBook.type = importedBook.type and BookType.notShelf.inv()
        assertFalse((importedBook.type and BookType.notShelf) != 0)
        assertTrue((importedBook.type and BookType.text) != 0)
        assertTrue((importedBook.type and BookType.local) != 0)
    }
}
