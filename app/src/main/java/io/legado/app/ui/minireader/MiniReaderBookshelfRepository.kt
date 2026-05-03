package io.legado.app.ui.minireader

import android.net.Uri
import io.legado.app.constant.BookType
import io.legado.app.data.entities.Book
import io.legado.app.help.book.removeType
import io.legado.app.model.localBook.LocalBook

class MiniReaderBookshelfRepository(
    private val importLocalBook: (Uri) -> Book = LocalBook::importFile,
    private val persistBook: (Book) -> Unit = { it.save() }
) {

    fun importFromPickedUri(uri: Uri): Book {
        return importFromPickedUriString(uri.toString()) { _ ->
            importLocalBook(uri)
        }
    }

    internal fun importFromPickedUriString(
        uriString: String,
        importBookAction: ((String) -> Book)? = null
    ): Book {
        val book = importBookAction?.invoke(uriString) ?: importLocalBook(Uri.parse(uriString))
        return finalizeImportedBook(book)
    }

    internal fun finalizeImportedBook(book: Book): Book {
        book.removeType(BookType.notShelf)
        persistBook(book)
        return book
    }
}
