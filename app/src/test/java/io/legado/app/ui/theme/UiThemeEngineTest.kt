package io.legado.app.ui.theme

import io.legado.app.lib.theme.system.UiThemeDefaults
import io.legado.app.lib.theme.system.UiThemeEngine
import io.legado.app.lib.theme.system.UiThemeSnapshotInput
import org.junit.Assert.assertEquals
import org.junit.Test

class UiThemeEngineTest {

    @Test
    fun buildSnapshot_returnsDefaults_whenInputMissing() {
        val snapshot = UiThemeEngine.buildSnapshot(null)

        assertEquals(UiThemeDefaults.deepImmersiveSnapshot, snapshot)
    }

    @Test
    fun buildSnapshot_preservesExplicitAccent_whenProvided() {
        val explicitAccent = 0xFF11AA77.toInt()

        val snapshot = UiThemeEngine.buildSnapshot(
            UiThemeSnapshotInput(
                accentColor = explicitAccent
            )
        )

        assertEquals(explicitAccent, snapshot.accentColor)
    }
}
