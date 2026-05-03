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
    fun import_from_picked_uri_should_delegate_uri_import_and_remove_not_shelf_flag() {
        val pickedUriString = "content://com.example.provider/document/book.txt"
        val importedBook = Book(
            bookUrl = pickedUriString,
            type = BookType.text or BookType.local or BookType.notShelf
        )
        var delegatedUriString: String? = null
        var persistedBook: Book? = null
        val repository = MiniReaderBookshelfRepository(
            importLocalBook = {
                throw AssertionError("default importLocalBook should not be called in this unit test")
            },
            persistBook = {
                persistedBook = it
            }
        )

        val result = repository.importFromPickedUriString(pickedUriString) { uriString ->
            delegatedUriString = uriString
            importedBook
        }

        assertEquals(importedBook, result)
        assertEquals(pickedUriString, delegatedUriString)
        assertEquals(pickedUriString, result.bookUrl)
        assertFalse(result.isType(BookType.notShelf))
        assertTrue(result.isType(BookType.text))
        assertTrue(result.isType(BookType.local))
        assertEquals(result, persistedBook)
    }
}
