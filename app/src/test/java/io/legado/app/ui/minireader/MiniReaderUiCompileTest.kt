package io.legado.app.ui.minireader

import io.legado.app.ui.minireader.ui.MiniReaderScreen
import org.junit.Assert.assertNotNull
import org.junit.Test

class MiniReaderUiCompileTest {

    @Test
    fun reader_screen_should_compile_and_accept_state_contract() {
        assertNotNull(::MiniReaderScreen)
    }
}
