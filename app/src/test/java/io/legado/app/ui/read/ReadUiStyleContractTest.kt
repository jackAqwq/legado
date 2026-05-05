package io.legado.app.ui.read

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReadUiStyleContractTest {

    @Test
    fun readMenuUsesUiThemeTokens() {
        val menuLayout = File("src/main/res/layout/view_read_menu.xml").readText()
        val searchLayout = File("src/main/res/layout/view_search_menu.xml").readText()
        val readMenuCode = File("src/main/java/io/legado/app/ui/book/read/ReadMenu.kt").readText()
        val searchMenuCode = File("src/main/java/io/legado/app/ui/book/read/SearchMenu.kt").readText()
        assertTrue(menuLayout.contains("@color/ui_surface_variant"))
        assertTrue(menuLayout.contains("@color/ui_on_surface"))
        assertTrue(searchLayout.contains("@color/ui_surface_variant"))
        assertTrue(searchLayout.contains("@color/ui_on_surface"))
        assertTrue(!menuLayout.contains("@color/primaryText"))
        assertTrue(!searchLayout.contains("@color/primaryText"))
        assertTrue(readMenuCode.contains("UiThemeSnapshotInput"))
        assertTrue(searchMenuCode.contains("UiThemeSnapshotInput"))
        assertTrue(searchMenuCode.contains("surfaceVariantColor"))
        assertTrue(searchMenuCode.contains("onSurfaceColor"))
    }
}
