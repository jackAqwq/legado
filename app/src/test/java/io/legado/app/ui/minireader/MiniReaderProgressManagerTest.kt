package io.legado.app.ui.minireader

import io.legado.app.constant.PreferKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MiniReaderProgressManagerTest {

    @Test
    fun save_and_restore_should_keep_global_offset_and_chapter_index() {
        val prefs = InMemoryMiniReaderPrefs()
        var fakeNow = 1000L
        val manager = MiniReaderProgressManager(
            prefs = prefs,
            nowProvider = { fakeNow }
        )

        val bookUrl = "content://books/demo.txt"
        val firstSaved = manager.saveProgress(bookUrl = bookUrl, chapterIndex = 3, globalOffset = 456)
        assertTrue(firstSaved)

        val progress = manager.loadProgress(bookUrl)
        assertEquals(3, progress.chapterIndex)
        assertEquals(456, progress.globalOffset)

        fakeNow += 100
        val throttled = manager.saveProgress(bookUrl = bookUrl, chapterIndex = 4, globalOffset = 600)
        assertFalse(throttled)

        fakeNow += 500
        val savedAfterWindow = manager.saveProgress(bookUrl = bookUrl, chapterIndex = 4, globalOffset = 600)
        assertTrue(savedAfterWindow)

        val latest = manager.loadProgress(bookUrl)
        assertEquals(4, latest.chapterIndex)
        assertEquals(600, latest.globalOffset)
    }

    @Test
    fun settings_should_persist_font_lineSpacing_bg_brightness() {
        val prefs = InMemoryMiniReaderPrefs()
        val manager = MiniReaderProgressManager(
            prefs = prefs,
            nowProvider = { 0L },
            defaultBgModeProvider = { MiniReaderProgressManager.BG_MODE_LIGHT }
        )

        val settings = MiniReaderProgressManager.MiniReaderSettings(
            fontSizeSp = 22,
            lineSpacingMultiplier = 1.6f,
            bgMode = MiniReaderProgressManager.BG_MODE_EYE_CARE,
            brightness = 88
        )
        manager.saveSettings(settings)

        val restored = manager.loadSettings()
        assertEquals(22, restored.fontSizeSp)
        assertEquals(1.6f, restored.lineSpacingMultiplier)
        assertEquals(MiniReaderProgressManager.BG_MODE_EYE_CARE, restored.bgMode)
        assertEquals(88, restored.brightness)

        assertEquals(22, prefs.getInt(PreferKey.miniReaderFontSize, -1))
        assertEquals(1.6f, prefs.getFloat(PreferKey.miniReaderLineSpacing, -1f), 0.0001f)
        assertEquals(MiniReaderProgressManager.BG_MODE_EYE_CARE, prefs.getInt(PreferKey.miniReaderBgMode, -1))
        assertEquals(88, prefs.getInt(PreferKey.miniReaderBrightness, -1))
    }

    @Test
    fun load_settings_should_use_injected_default_bg_mode_when_unset() {
        val prefs = InMemoryMiniReaderPrefs()
        val manager = MiniReaderProgressManager(
            prefs = prefs,
            nowProvider = { 0L },
            defaultBgModeProvider = { MiniReaderProgressManager.BG_MODE_EYE_CARE }
        )

        val restored = manager.loadSettings()

        assertEquals(MiniReaderProgressManager.BG_MODE_EYE_CARE, restored.bgMode)
    }
}

private class InMemoryMiniReaderPrefs : MiniReaderProgressManager.PrefStore {
    private val map = mutableMapOf<String, Any>()

    override fun getInt(key: String, defaultValue: Int): Int {
        return map[key] as? Int ?: defaultValue
    }

    override fun putInt(key: String, value: Int) {
        map[key] = value
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return map[key] as? Float ?: defaultValue
    }

    override fun putFloat(key: String, value: Float) {
        map[key] = value
    }
}
