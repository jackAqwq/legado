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
        assertFalse(
            "LiveData internals keep should be removed and rely on library consumer rules",
            rules.contains("androidx.lifecycle.LiveData")
                    && rules.contains("mObservers")
                    && rules.contains("mActiveCount")
        )
        assertFalse(
            "SafeIterableMap internals keep should be removed and rely on library consumer rules",
            rules.contains("androidx.arch.core.internal.SafeIterableMap")
                    && rules.contains("putIfAbsent")
        )
        assertFalse(
            "Broad tm4e keep should be removed when TextMate consumer rules are sufficient",
            rules.contains("-keep class org.eclipse.tm4e.** { *; }")
        )
        assertFalse(
            "Broad joni keep should be removed when TextMate consumer rules are sufficient",
            rules.contains("-keep class org.joni.** { *; }")
        )
        assertFalse(
            "CookieStore explicit keep should be removed and rely on @Keep consumer rules",
            rules.contains("-keep class **.help.http.CookieStore{*;}")
        )
        assertFalse(
            "CacheManager explicit keep should be removed and rely on @Keep consumer rules",
            rules.contains("-keep class **.help.CacheManager{*;}")
        )
        assertFalse(
            "StrResponse explicit keep should be removed and rely on @Keep consumer rules",
            rules.contains("-keep class **.help.http.StrResponse{*;}")
        )
        assertFalse(
            "ReturnData explicit keep should be removed and rely on @Keep consumer rules",
            rules.contains("-keep class io.legado.app.api.ReturnData{*;}")
        )
        assertFalse(
            "ExoPlayer CacheDataSource.Factory upstreamDataSourceFactory keep should stay removed after reflection cleanup",
            rules.contains("CacheDataSource\$Factory")
                    && rules.contains("upstreamDataSourceFactory")
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
            "Broad hutool core util keep should be removed",
            rules.contains("cn.hutool.core.util.**{*;}")
        )
        assertFalse(
            "Broad hutool crypto keep should be removed",
            rules.contains("-keep class cn.hutool.crypto.**{*;}")
        )
        assertTrue(
            "Narrow hutool keep should retain Base64 codec",
            rules.contains("cn.hutool.core.codec.Base64")
        )
        assertTrue(
            "Narrow hutool keep should retain SymmetricCrypto",
            rules.contains("cn.hutool.crypto.symmetric.SymmetricCrypto")
        )
        assertFalse(
            "Throwable member keep should be removed",
            rules.contains("-keepclassmembernames,allowobfuscation class * extends java.lang.Throwable{*;}")
        )
    }
}
