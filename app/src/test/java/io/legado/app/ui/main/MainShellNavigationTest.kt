package io.legado.app.ui.main

import io.legado.app.ui.main.shell.MainShellNavigator
import io.legado.app.ui.main.shell.MainShellTab
import io.legado.app.ui.main.shell.MainShellTabHost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainShellNavigationTest {

    @Test
    fun repeated_bookshelf_reselect_should_emit_event_once() {
        val host = FakeMainShellTabHost(MainShellTab.BOOKSHELF)
        var now = 1_000L
        val navigator = MainShellNavigator(
            tabHost = host,
            nowProvider = { now },
            bookshelfReselectWindowMs = 300L
        )

        navigator.onTabReselected(MainShellTab.BOOKSHELF)
        assertFalse(navigator.consumeBookshelfReselectEvent())

        now += 200L
        navigator.onTabReselected(MainShellTab.BOOKSHELF)
        assertTrue(navigator.consumeBookshelfReselectEvent())
        assertFalse(navigator.consumeBookshelfReselectEvent())
    }

    @Test
    fun back_press_from_non_home_should_switch_to_bookshelf_first() {
        val host = FakeMainShellTabHost(MainShellTab.MY)
        val navigator = MainShellNavigator(tabHost = host)

        assertTrue(navigator.onBackPressed())
        assertEquals(MainShellTab.BOOKSHELF, host.currentTab())
        assertEquals(listOf(MainShellTab.BOOKSHELF), host.showHistory)
        assertFalse(navigator.onBackPressed())
    }

    private class FakeMainShellTabHost(
        private var current: MainShellTab
    ) : MainShellTabHost {

        val showHistory = mutableListOf<MainShellTab>()

        override fun showTab(tab: MainShellTab) {
            current = tab
            showHistory.add(tab)
        }

        override fun currentTab(): MainShellTab {
            return current
        }
    }
}
