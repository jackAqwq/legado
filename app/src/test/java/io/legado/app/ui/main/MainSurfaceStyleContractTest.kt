package io.legado.app.ui.main

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MainSurfaceStyleContractTest {

    @Test
    fun mainFragmentsUseUiSurfaceTokens() {
        val files = listOf(
            "src/main/res/layout/fragment_bookshelf1.xml",
            "src/main/res/layout/fragment_bookshelf2.xml",
            "src/main/res/layout/fragment_explore.xml",
            "src/main/res/layout/fragment_rss.xml",
            "src/main/res/layout/fragment_my_config.xml"
        )
        files.forEach { path ->
            val text = File(path).readText()
            assertTrue(
                "$path should use ui surface background",
                text.contains("@color/ui_surface")
            )
            assertTrue(
                "$path should use card surface shell",
                text.contains("@drawable/bg_app_card_surface")
            )
        }
    }
}
