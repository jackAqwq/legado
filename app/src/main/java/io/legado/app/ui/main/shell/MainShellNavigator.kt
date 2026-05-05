package io.legado.app.ui.main.shell

class MainShellNavigator(
    private val tabHost: MainShellTabHost,
) {

    private var hasPendingBookshelfReselectEvent = false

    enum class BackResult {
        SWITCHED_HOME,
        SHOULD_EXIT
    }

    fun select(tab: MainShellTab) {
        tabHost.showTab(tab)
    }

    fun onTabReselected(tab: MainShellTab) {
        if (tab == MainShellTab.BOOKSHELF) {
            hasPendingBookshelfReselectEvent = true
        }
    }

    fun consumeBookshelfReselectEvent(): Boolean {
        if (!hasPendingBookshelfReselectEvent) {
            return false
        }
        hasPendingBookshelfReselectEvent = false
        return true
    }

    fun onBackPressed(): BackResult {
        if (tabHost.currentTab() == MainShellTab.BOOKSHELF) {
            return BackResult.SHOULD_EXIT
        }
        tabHost.showTab(MainShellTab.BOOKSHELF)
        return BackResult.SWITCHED_HOME
    }
}
