package io.legado.app.ui.book.read.page

internal enum class RenderInvalidateMode {
    NONE,
    INVALIDATE,
    POST_INVALIDATE
}

internal data class RenderInvalidateDecision(
    val viewMode: RenderInvalidateMode,
    val invalidateDelegate: Boolean
)

internal object RenderInvalidateGate {

    fun forContentUpdate(isScroll: Boolean): RenderInvalidateDecision {
        return RenderInvalidateDecision(
            viewMode = if (isScroll) {
                RenderInvalidateMode.POST_INVALIDATE
            } else {
                RenderInvalidateMode.INVALIDATE
            },
            invalidateDelegate = false
        )
    }

    fun forPreRender(
        hasUpdates: Boolean,
        delegateCanInvalidate: Boolean
    ): RenderInvalidateDecision {
        if (!hasUpdates) {
            return RenderInvalidateDecision(
                viewMode = RenderInvalidateMode.NONE,
                invalidateDelegate = false
            )
        }
        return RenderInvalidateDecision(
            viewMode = RenderInvalidateMode.POST_INVALIDATE,
            invalidateDelegate = delegateCanInvalidate
        )
    }
}
