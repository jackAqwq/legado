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
        assertFalse(
            "Broad GSYVideoPlayer keep should be removed",
            rules.contains("-keep class com.shuyu.gsyvideoplayer.** { *; }")
        )
        assertFalse(
            "Toolbar private-field keep should be removed after reflection cleanup",
            rules.contains("androidx.appcompat.widget.Toolbar")
                    && rules.contains("mNavButtonView")
        )
        assertFalse(
            "MenuBuilder keep should be removed after reflection cleanup",
            rules.contains("-keep class androidx.appcompat.view.menu.MenuBuilder")
        )
        assertFalse(
            "SubMenuBuilder keepnames should be removed after reflection cleanup",
            rules.contains("-keepnames class androidx.appcompat.view.menu.SubMenuBuilder")
        )
        assertTrue(
            "App custom video wrappers should still be kept",
            rules.contains("-keep class io.legado.app.help.gsyVideo.FloatingPlayer")
        )
        assertTrue(
            "Throwable class names should remain stable for logs/crash readability",
            rules.contains("-keepnames class * extends java.lang.Throwable")
        )
        assertFalse(
            "Throwable member keep should be removed",
            rules.contains("-keepclassmembernames,allowobfuscation class * extends java.lang.Throwable{*;}")
        )
    }
}
