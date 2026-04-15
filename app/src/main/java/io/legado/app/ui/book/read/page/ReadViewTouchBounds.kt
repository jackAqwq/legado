package io.legado.app.ui.book.read.page

internal object ReadViewTouchBounds {
    fun shouldIgnoreTouchForMandatoryGestures(
        y: Float,
        viewHeight: Int,
        insetBottom: Int
    ): Boolean {
        if (insetBottom <= 0 || viewHeight <= 0) return false
        val limit = viewHeight - insetBottom
        return y > limit
    }
}
