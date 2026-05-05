package io.legado.app.ui.main.shell

class MainShellNavigator(
    private val tabHost: MainShellTabHost,
    private val nowProvider: () -> Long = { System.currentTimeMillis() },
    private val bookshelfReselectWindowMs: Long = 300L
) {

    private var lastBookshelfReselectAt = 0L
    private var hasPendingBookshelfReselectEvent = false

    fun select(tab: MainShellTab) {
        tabHost.showTab(tab)
    }

    fun onTabReselected(tab: MainShellTab) {
        if (tab != MainShellTab.BOOKSHELF) {
            return
        }
        val now = nowProvider()
        if (now - lastBookshelfReselectAt <= bookshelfReselectWindowMs) {
            hasPendingBookshelfReselectEvent = true
            lastBookshelfReselectAt = 0L
            return
        }
        lastBookshelfReselectAt = now
    }

    fun consumeBookshelfReselectEvent(): Boolean {
        if (!hasPendingBookshelfReselectEvent) {
            return false
        }
        hasPendingBookshelfReselectEvent = false
        return true
    }

    fun onBackPressed(): Boolean {
        if (tabHost.currentTab() == MainShellTab.BOOKSHELF) {
            return false
        }
        tabHost.showTab(MainShellTab.BOOKSHELF)
        return true
    }
}
