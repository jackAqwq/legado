package io.legado.app.model

import kotlin.math.max

internal object ReadBookLoadDecider {

    fun isChapterInRenderWindow(chapterIndex: Int, durChapterIndex: Int): Boolean {
        return chapterIndex in (durChapterIndex - 1)..(durChapterIndex + 1)
    }

    fun shouldUpdateForScroll(
        upContent: Boolean,
        isScrollMode: Boolean,
        layoutPageIndex: Int,
        durPageIndex: Int
    ): Boolean {
        if (!upContent || !isScrollMode) return false
        return max(layoutPageIndex - 3, 0) < durPageIndex
    }

    fun shouldRenderNextPreviewPage(layoutPageIndex: Int): Boolean {
        return layoutPageIndex <= 1
    }
}
