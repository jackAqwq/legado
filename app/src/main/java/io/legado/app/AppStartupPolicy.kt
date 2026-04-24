package io.legado.app

import java.util.concurrent.TimeUnit

internal enum class ChineseConverterPreloadMode {
    TRADITIONAL_TO_SIMPLE,
    SIMPLE_TO_TRADITIONAL
}

internal object AppStartupPolicy {

    private val startupBookProgressSyncIntervalMs = TimeUnit.MINUTES.toMillis(30)

    fun shouldRunStartupBookProgressSync(now: Long, lastSyncTime: Long): Boolean {
        return now - lastSyncTime >= startupBookProgressSyncIntervalMs
    }

    fun shouldClearExpiredSearchBooks(autoClearExpired: Boolean): Boolean {
        return autoClearExpired
    }

    fun resolveChineseConverterPreloadMode(
        chineseConverterType: Int
    ): ChineseConverterPreloadMode? {
        return when (chineseConverterType) {
            1 -> ChineseConverterPreloadMode.TRADITIONAL_TO_SIMPLE
            2 -> ChineseConverterPreloadMode.SIMPLE_TO_TRADITIONAL
            else -> null
        }
    }
}
