package io.legado.app.ui.book.read.page

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadViewTouchBoundsTest {

    @Test
    fun should_not_ignore_when_inset_is_zero_or_height_invalid() {
        assertFalse(
            ReadViewTouchBounds.shouldIgnoreTouchForMandatoryGestures(
                y = 999f,
                viewHeight = 1000,
                insetBottom = 0
            )
        )
        assertFalse(
            ReadViewTouchBounds.shouldIgnoreTouchForMandatoryGestures(
                y = 999f,
                viewHeight = 0,
                insetBottom = 100
            )
        )
    }

    @Test
    fun should_ignore_only_when_touch_is_below_mandatory_gesture_limit() {
        // limit = 1000 - 100 = 900
        assertFalse(
            ReadViewTouchBounds.shouldIgnoreTouchForMandatoryGestures(
                y = 900f,
                viewHeight = 1000,
                insetBottom = 100
            )
        )
        assertTrue(
            ReadViewTouchBounds.shouldIgnoreTouchForMandatoryGestures(
                y = 900.1f,
                viewHeight = 1000,
                insetBottom = 100
            )
        )
    }
}
