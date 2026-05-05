package io.legado.app.ui.dialog

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AppDialogStyleContractTest {

    @Test
    fun highFrequencyDialogsUseUiCardShell() {
        val dialogLayouts = listOf(
            "src/main/res/layout/dialog_text_view.xml",
            "src/main/res/layout/dialog_variable.xml",
            "src/main/res/layout/dialog_url_option_edit.xml",
            "src/main/res/layout/dialog_wait.xml"
        )
        dialogLayouts.forEach { path ->
            val text = File(path).readText()
            assertTrue(
                "$path should use ui tokens",
                text.contains("@drawable/bg_app_card_surface") || text.contains("@color/ui_surface")
            )
        }
    }

    @Test
    fun toolbarDialogsUseThemeSnapshotPrimary() {
        val textDialogCode =
            File("src/main/java/io/legado/app/ui/widget/dialog/TextDialog.kt").readText()
        val variableDialogCode =
            File("src/main/java/io/legado/app/ui/widget/dialog/VariableDialog.kt").readText()
        assertTrue(textDialogCode.contains("UiThemeSnapshotInput"))
        assertTrue(textDialogCode.contains("uiSnapshot.primaryColor"))
        assertTrue(variableDialogCode.contains("UiThemeSnapshotInput"))
        assertTrue(variableDialogCode.contains("uiSnapshot.primaryColor"))
    }
}
