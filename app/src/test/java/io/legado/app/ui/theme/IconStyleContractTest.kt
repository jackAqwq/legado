package io.legado.app.ui.theme

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class IconStyleContractTest {

    @Test
    fun bottomNavIconsUseUiSemanticColors() {
        val selected = listOf(
            "src/main/res/drawable/ic_bottom_books_s.xml",
            "src/main/res/drawable/ic_bottom_explore_s.xml",
            "src/main/res/drawable/ic_bottom_rss_feed_s.xml",
            "src/main/res/drawable/ic_bottom_person_s.xml",
        )
        val unselected = listOf(
            "src/main/res/drawable/ic_bottom_books_e.xml",
            "src/main/res/drawable/ic_bottom_explore_e.xml",
            "src/main/res/drawable/ic_bottom_rss_feed_e.xml",
            "src/main/res/drawable/ic_bottom_person_e.xml",
        )
        selected.forEach { path ->
            val text = File(path).readText()
            assertTrue("$path should use ui_primary", text.contains("@color/ui_primary"))
            assertTrue("$path should not hardcode legacy blue", !text.contains("#2f45a6"))
        }
        unselected.forEach { path ->
            val text = File(path).readText()
            assertTrue("$path should use ui_on_surface_muted", text.contains("@color/ui_on_surface_muted"))
            assertTrue("$path should not hardcode legacy blue", !text.contains("#2f45a6"))
        }
    }
}
