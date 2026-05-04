package io.legado.app.manifest

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProguardEntityKeepRulesGuardTest {

    @Test
    fun entityKeepRuleHasCompatibilityNote() {
        val rules = File("proguard-rules.pro").readText()
        assertTrue(
            "Entity keep block should carry compatibility note before staged narrowing",
            rules.contains("data.entities currently remains package-wide")
        )
        assertTrue(
            "Entity package keep should stay until staged per-entity rollout is complete",
            rules.contains("-keep class **.data.entities.**{*;}")
        )
    }
}
