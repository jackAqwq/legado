package io.legado.app.manifest

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UiOverhaulSourceGuardTest {

    @Test
    fun stylesReferenceUiOverhaulAssets() {
        val styles = File("src/main/res/values/styles.xml").readText()
        assertTrue(
            "styles.xml should reference ui overhaul card background",
            styles.contains("@drawable/bg_app_card_surface")
        )
        assertTrue(
            "styles.xml should reference ui surface token",
            styles.contains("@color/ui_surface")
        )
    }
}
