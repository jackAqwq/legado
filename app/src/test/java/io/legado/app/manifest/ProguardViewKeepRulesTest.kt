package io.legado.app.manifest

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProguardViewKeepRulesTest {

    @Test
    fun customViewKeepRulesAreScoped() {
        val rules = File("proguard-rules.pro").readText()
        assertFalse(
            "Global keep for every View subclass must not be used",
            rules.contains("-keep public class * extends android.view.View")
        )
        assertTrue(
            "Expected scoped keep for app custom widgets",
            rules.contains("-keep class io.legado.app.ui.widget.**")
        )
        assertTrue(
            "Expected scoped keep for read pages",
            rules.contains("-keep class io.legado.app.ui.book.read.page.**")
        )
        assertTrue(
            "Expected Jsoup keep to allow optimization while preserving runtime script compatibility",
            rules.contains("-keep,allowoptimization class org.jsoup.** { *; }")
        )
        assertFalse(
            "TreeDocumentFile keep should be removed",
            rules.contains("androidx.documentfile.provider.TreeDocumentFile")
        )
    }
}
