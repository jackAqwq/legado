package io.legado.app.ui.book.read.page

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RenderInvalidateGateTest {

    @Test
    fun for_content_update_should_use_post_invalidate_for_scroll_mode() {
        val decision = RenderInvalidateGate.forContentUpdate(isScroll = true)

        assertEquals(RenderInvalidateMode.POST_INVALIDATE, decision.viewMode)
        assertFalse(decision.invalidateDelegate)
    }

    @Test
    fun for_content_update_should_use_direct_invalidate_for_paged_mode() {
        val decision = RenderInvalidateGate.forContentUpdate(isScroll = false)

        assertEquals(RenderInvalidateMode.INVALIDATE, decision.viewMode)
        assertFalse(decision.invalidateDelegate)
    }

    @Test
    fun for_pre_render_should_skip_everything_when_nothing_changed() {
        val decision = RenderInvalidateGate.forPreRender(
            hasUpdates = false,
            delegateCanInvalidate = true
        )

        assertEquals(RenderInvalidateMode.NONE, decision.viewMode)
        assertFalse(decision.invalidateDelegate)
    }

    @Test
    fun for_pre_render_should_request_delegate_only_when_delegate_can_redraw() {
        val withDelegate = RenderInvalidateGate.forPreRender(
            hasUpdates = true,
            delegateCanInvalidate = true
        )
        val withoutDelegate = RenderInvalidateGate.forPreRender(
            hasUpdates = true,
            delegateCanInvalidate = false
        )

        assertEquals(RenderInvalidateMode.POST_INVALIDATE, withDelegate.viewMode)
        assertTrue(withDelegate.invalidateDelegate)
        assertEquals(RenderInvalidateMode.POST_INVALIDATE, withoutDelegate.viewMode)
        assertFalse(withoutDelegate.invalidateDelegate)
    }
}
