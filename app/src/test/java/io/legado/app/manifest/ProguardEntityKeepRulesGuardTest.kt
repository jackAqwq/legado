package io.legado.app.manifest

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProguardEntityKeepRulesGuardTest {

    @Test
    fun entityKeepRulesAreExplicitlyScoped() {
        val rules = File("proguard-rules.pro").readText()
        assertTrue(
            "Entity keep block should carry explicit-scope compatibility note",
            rules.contains("Narrowed from package-wide to explicit models")
        )
        assertFalse(
            "Broad package-wide entity keep should be removed",
            rules.contains("-keep class **.data.entities.**{*;}")
        )
        assertTrue(
            "Expected explicit keep for Book entity",
            rules.contains("-keep class io.legado.app.data.entities.Book { *; }")
        )
        assertTrue(
            "Expected explicit keep for RssSource entity",
            rules.contains("-keep class io.legado.app.data.entities.RssSource { *; }")
        )
        assertTrue(
            "Expected explicit keep for entity rule models",
            rules.contains("-keep class io.legado.app.data.entities.rule.** { *; }")
        )
        assertFalse(
            "DB-only cache entity should not be blanket-kept",
            rules.contains("-keep class io.legado.app.data.entities.Cache { *; }")
        )
        assertFalse(
            "DB-only cookie entity should not be blanket-kept",
            rules.contains("-keep class io.legado.app.data.entities.Cookie { *; }")
        )
        assertFalse(
            "RSS read record entity should not be blanket-kept when no external JSON contract requires it",
            rules.contains("-keep class io.legado.app.data.entities.RssReadRecord { *; }")
        )
        assertFalse(
            "RuleSub entity should not be blanket-kept when no external JSON contract requires it",
            rules.contains("-keep class io.legado.app.data.entities.RuleSub { *; }")
        )
    }
}
