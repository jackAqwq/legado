package io.legado.app.ui.main

import org.junit.Assert.assertEquals
import org.junit.Test

class MainStartupTaskPlannerTest {

    @Test
    fun plan_should_include_all_deferred_tasks_when_auto_refresh_is_needed() {
        val tasks = MainStartupTaskPlanner.plan(
            autoRefreshBook = true,
            alreadyAutoRefreshedBook = false
        )

        assertEquals(
            listOf(
                MainStartupTask(MainStartupTaskType.RULE_SUBS_UPDATE, 1000L),
                MainStartupTask(MainStartupTaskType.AUTO_REFRESH_BOOK, 2000L),
                MainStartupTask(MainStartupTaskType.POST_LOAD, 3000L)
            ),
            tasks
        )
    }

    @Test
    fun plan_should_skip_auto_refresh_when_already_done_or_disabled() {
        val disabled = MainStartupTaskPlanner.plan(
            autoRefreshBook = false,
            alreadyAutoRefreshedBook = false
        )
        val alreadyDone = MainStartupTaskPlanner.plan(
            autoRefreshBook = true,
            alreadyAutoRefreshedBook = true
        )

        assertEquals(
            listOf(
                MainStartupTask(MainStartupTaskType.RULE_SUBS_UPDATE, 1000L),
                MainStartupTask(MainStartupTaskType.POST_LOAD, 3000L)
            ),
            disabled
        )
        assertEquals(disabled, alreadyDone)
    }
}
