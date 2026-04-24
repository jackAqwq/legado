package io.legado.app.ui.main

internal enum class MainStartupTaskType {
    RULE_SUBS_UPDATE,
    AUTO_REFRESH_BOOK,
    POST_LOAD
}

internal data class MainStartupTask(
    val type: MainStartupTaskType,
    val delayMs: Long
)

internal object MainStartupTaskPlanner {

    private const val RULE_SUBS_DELAY_MS = 1000L
    private const val AUTO_REFRESH_BOOK_DELAY_MS = 2000L
    private const val POST_LOAD_DELAY_MS = 3000L

    fun plan(
        autoRefreshBook: Boolean,
        alreadyAutoRefreshedBook: Boolean
    ): List<MainStartupTask> {
        val tasks = mutableListOf(
            MainStartupTask(MainStartupTaskType.RULE_SUBS_UPDATE, RULE_SUBS_DELAY_MS)
        )
        if (autoRefreshBook && !alreadyAutoRefreshedBook) {
            tasks.add(
                MainStartupTask(
                    MainStartupTaskType.AUTO_REFRESH_BOOK,
                    AUTO_REFRESH_BOOK_DELAY_MS
                )
            )
        }
        tasks.add(
            MainStartupTask(MainStartupTaskType.POST_LOAD, POST_LOAD_DELAY_MS)
        )
        return tasks
    }
}
