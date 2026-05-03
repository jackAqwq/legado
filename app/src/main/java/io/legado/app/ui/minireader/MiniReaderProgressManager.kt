package io.legado.app.ui.minireader

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import io.legado.app.constant.PreferKey
import io.legado.app.help.config.AppConfig
import io.legado.app.utils.putFloat
import java.util.concurrent.atomic.AtomicLong

class MiniReaderProgressManager(
    context: Context? = null,
    private val prefs: PrefStore = AndroidPrefStore(
        checkNotNull(context) { "context is required when prefs is not provided" }
            .getSharedPreferences(PREF_NAME, MODE_PRIVATE)
    ),
    private val nowProvider: () -> Long = { System.currentTimeMillis() },
    private val defaultBgModeProvider: () -> Int = { AppConfig.miniReaderDefaultBgMode }
) {

    interface PrefStore {
        fun getInt(key: String, defaultValue: Int): Int
        fun putInt(key: String, value: Int)
        fun getFloat(key: String, defaultValue: Float): Float
        fun putFloat(key: String, value: Float)
    }

    data class MiniReaderProgress(
        val chapterIndex: Int,
        val globalOffset: Int
    )

    data class MiniReaderSettings(
        val fontSizeSp: Int,
        val lineSpacingMultiplier: Float,
        val bgMode: Int,
        val brightness: Int
    )

    private val lastSaveMs = AtomicLong(0L)

    fun saveProgress(bookUrl: String, chapterIndex: Int, globalOffset: Int, force: Boolean = false): Boolean {
        val now = nowProvider()
        val last = lastSaveMs.get()
        if (!force && now - last < PROGRESS_WRITE_THROTTLE_MS) {
            return false
        }
        prefs.putInt(progressChapterKey(bookUrl), chapterIndex)
        prefs.putInt(progressOffsetKey(bookUrl), globalOffset)
        lastSaveMs.set(now)
        return true
    }

    fun loadProgress(bookUrl: String): MiniReaderProgress {
        return MiniReaderProgress(
            chapterIndex = prefs.getInt(progressChapterKey(bookUrl), 0),
            globalOffset = prefs.getInt(progressOffsetKey(bookUrl), 0)
        )
    }

    fun saveSettings(settings: MiniReaderSettings) {
        prefs.putInt(PreferKey.miniReaderFontSize, settings.fontSizeSp)
        prefs.putFloat(PreferKey.miniReaderLineSpacing, settings.lineSpacingMultiplier)
        prefs.putInt(PreferKey.miniReaderBgMode, settings.bgMode)
        prefs.putInt(PreferKey.miniReaderBrightness, settings.brightness)
    }

    fun loadSettings(): MiniReaderSettings {
        return MiniReaderSettings(
            fontSizeSp = prefs.getInt(PreferKey.miniReaderFontSize, DEFAULT_FONT_SIZE_SP),
            lineSpacingMultiplier = prefs.getFloat(PreferKey.miniReaderLineSpacing, DEFAULT_LINE_SPACING),
            bgMode = prefs.getInt(PreferKey.miniReaderBgMode, defaultBgModeProvider()),
            brightness = prefs.getInt(PreferKey.miniReaderBrightness, DEFAULT_BRIGHTNESS)
        )
    }

    private fun progressChapterKey(bookUrl: String): String {
        return "${PreferKey.miniReaderProgressPrefix}${bookUrl}:chapter"
    }

    private fun progressOffsetKey(bookUrl: String): String {
        return "${PreferKey.miniReaderProgressPrefix}${bookUrl}:offset"
    }

    companion object {
        private const val PREF_NAME = "mini_reader_config"
        private const val PROGRESS_WRITE_THROTTLE_MS = 250L

        const val BG_MODE_LIGHT = 0
        const val BG_MODE_EYE_CARE = 1

        const val DEFAULT_FONT_SIZE_SP = 18
        const val DEFAULT_LINE_SPACING = 1.35f
        const val DEFAULT_BRIGHTNESS = 100
    }
}

private class AndroidPrefStore(
    private val prefs: SharedPreferences
) : MiniReaderProgressManager.PrefStore {

    override fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }

    override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return prefs.getFloat(key, defaultValue)
    }

    override fun putFloat(key: String, value: Float) {
        prefs.putFloat(key, value)
    }
}

