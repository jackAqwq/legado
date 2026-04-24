package io.legado.app.help

import android.webkit.JavascriptInterface

@Suppress("unused")
object WebCacheManager {

    @JavascriptInterface
    @JvmOverloads
    fun put(key: String, value: String, saveTime: Int = 0) {
        CacheManager.put(key, value, saveTime)
    }

    @JavascriptInterface
    fun get(key: String): String? = CacheManager.get(key)

    @JavascriptInterface
    fun delete(key: String) {
        CacheManager.delete(key)
    }

    @JavascriptInterface
    @JvmOverloads
    fun putFile(key: String, value: String, saveTime: Int = 0) {
        CacheManager.putFile(key, value, saveTime)
    }

    @JavascriptInterface
    fun getFile(key: String): String? = CacheManager.getFile(key)

    @JavascriptInterface
    fun putMemory(key: String, value: String) {
        CacheManager.putMemory(key, value)
    }

    @JavascriptInterface
    fun getFromMemory(key: String): Any? = CacheManager.getFromMemory(key)

    @JavascriptInterface
    fun deleteMemory(key: String) {
        CacheManager.deleteMemory(key)
    }
}
