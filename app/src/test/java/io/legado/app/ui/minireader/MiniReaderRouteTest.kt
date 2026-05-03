package io.legado.app.ui.minireader

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MiniReaderRouteTest {

    @Test
    fun bookshelf_menu_should_expose_minireader_import_action() {
        val menuFile = File("src/main/res/menu/main_bookshelf.xml")
        val content = menuFile.readText()
        assertTrue(content.contains("menu_add_local_minireader"))
        assertTrue(content.contains("@string/mini_reader_local"))
    }

    @Test
    fun mini_reader_route_should_put_book_url_extra() {
        val extFile = File("src/main/java/io/legado/app/utils/ContextExtensions.kt")
        val content = extFile.readText()
        assertTrue(content.contains("MiniReaderActivity::class.java"))
        assertTrue(content.contains("MiniReaderContract.EXTRA_BOOK_URL"))
        assertTrue(content.contains("putExtra(MiniReaderContract.EXTRA_BOOK_URL, book.bookUrl)"))
    }
}
