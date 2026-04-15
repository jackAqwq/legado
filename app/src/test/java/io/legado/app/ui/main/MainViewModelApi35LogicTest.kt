package io.legado.app.ui.main

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class MainViewModelApi35LogicTest {

    @Test
    fun updating_book_count_is_sum_of_wait_and_updating() {
        assertEquals(0, MainViewModelApi35Logic.updatingBookCount(0, 0))
        assertEquals(5, MainViewModelApi35Logic.updatingBookCount(2, 3))
        assertEquals(9, MainViewModelApi35Logic.updatingBookCount(9, 0))
    }

    @Test
    fun cache_book_enabled_only_when_both_sets_are_empty() {
        assertTrue(MainViewModelApi35Logic.shouldEnableCacheBook(true, true))
        assertFalse(MainViewModelApi35Logic.shouldEnableCacheBook(false, true))
        assertFalse(MainViewModelApi35Logic.shouldEnableCacheBook(true, false))
        assertFalse(MainViewModelApi35Logic.shouldEnableCacheBook(false, false))
    }
}
