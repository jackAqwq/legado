package io.legado.app.ui.book.read.config

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PageKeyDialogContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun resetClearsInputsAndConfirmReturnsLatestValues() {
        var prevKeys by mutableStateOf("24,25")
        var nextKeys by mutableStateOf("92")
        var confirmedPrev = ""
        var confirmedNext = ""

        composeRule.setContent {
            PageKeyDialogContent(
                prevKeys = prevKeys,
                nextKeys = nextKeys,
                colors = PageKeyDialogColors.preview(),
                onPrevKeysChange = { prevKeys = it },
                onNextKeysChange = { nextKeys = it },
                onPrevHardwareKey = { prevKeys = appendPageKeyCode(prevKeys, it) },
                onNextHardwareKey = { nextKeys = appendPageKeyCode(nextKeys, it) },
                onResetClick = {
                    prevKeys = ""
                    nextKeys = ""
                },
                onConfirmClick = {
                    confirmedPrev = prevKeys
                    confirmedNext = nextKeys
                },
            )
        }

        composeRule.onNodeWithTag("page_key_reset").performClick()
        composeRule.onNodeWithTag("page_key_prev_field").assertTextEquals("")
        composeRule.onNodeWithTag("page_key_next_field").assertTextEquals("")

        composeRule.onNodeWithTag("page_key_prev_field").performTextInput("24")
        composeRule.onNodeWithTag("page_key_next_field").performTextInput("92")
        composeRule.onNodeWithTag("page_key_confirm").performClick()

        composeRule.runOnIdle {
            assertEquals("24", confirmedPrev)
            assertEquals("92", confirmedNext)
        }
    }
}
