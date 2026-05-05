package io.legado.app.ui.main.shell

enum class MainShellTab {
    BOOKSHELF,
    EXPLORE,
    RSS,
    MY
}

interface MainShellTabHost {
    fun showTab(tab: MainShellTab)
    fun currentTab(): MainShellTab
}
