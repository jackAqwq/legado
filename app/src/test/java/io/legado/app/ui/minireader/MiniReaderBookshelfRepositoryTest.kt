package io.legado.app.ui.minireader

import io.legado.app.constant.BookType
import io.legado.app.data.entities.Book
import io.legado.app.help.book.isType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MiniReaderBookshelfRepositoryTest {

    @Test
    fun import_from_picked_uri_should_keep_original_content_uri_and_remove_not_shelf_flag() {
        val importedBook = Book(
            bookUrl = "content://legacy/other-path.txt",
            type = BookType.text or BookType.local or BookType.notShelf
        )
        var persistedBook: Book? = null
        val repository = MiniReaderBookshelfRepository(
            importLocalBook = { importedBook },
            persistBook = {
                persistedBook = it
            }
        )

        val result = repository.finalizeImportedBook(importedBook)

        assertEquals(importedBook, result)
        assertEquals("content://legacy/other-path.txt", result.bookUrl)
        assertFalse(result.isType(BookType.notShelf))
        assertTrue(result.isType(BookType.text))
        assertTrue(result.isType(BookType.local))
        assertEquals(result, persistedBook)
    }
}
