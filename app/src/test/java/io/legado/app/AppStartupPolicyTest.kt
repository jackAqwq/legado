package io.legado.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStartupPolicyTest {

    @Test
    fun should_run_startup_book_progress_sync_only_after_interval() {
        assertTrue(
            AppStartupPolicy.shouldRunStartupBookProgressSync(
                now = 60_000L * 31,
                lastSyncTime = 0L
            )
        )
        assertFalse(
            AppStartupPolicy.shouldRunStartupBookProgressSync(
                now = 60_000L * 10,
                lastSyncTime = 0L
            )
        )
    }

    @Test
    fun should_clear_expired_search_books_only_when_enabled() {
        assertTrue(AppStartupPolicy.shouldClearExpiredSearchBooks(autoClearExpired = true))
        assertFalse(AppStartupPolicy.shouldClearExpiredSearchBooks(autoClearExpired = false))
    }

    @Test
    fun resolve_chinese_converter_preload_mode_should_match_existing_mapping() {
        assertEquals(
            ChineseConverterPreloadMode.TRADITIONAL_TO_SIMPLE,
            AppStartupPolicy.resolveChineseConverterPreloadMode(1)
        )
        assertEquals(
            ChineseConverterPreloadMode.SIMPLE_TO_TRADITIONAL,
            AppStartupPolicy.resolveChineseConverterPreloadMode(2)
        )
        assertEquals(null, AppStartupPolicy.resolveChineseConverterPreloadMode(0))
    }
}
