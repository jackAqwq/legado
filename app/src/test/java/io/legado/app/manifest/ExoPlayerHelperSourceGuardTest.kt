package io.legado.app.manifest

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class ExoPlayerHelperSourceGuardTest {

    @Test
    fun noLegacyUpstreamFieldReflectionRemains() {
        val source = File("src/main/java/io/legado/app/help/exoplayer/ExoPlayerHelper.kt").readText()
        assertFalse(
            "Legacy upstreamDataSourceFactory reflection should remain removed",
            source.contains("getDeclaredField(\"upstreamDataSourceFactory\")")
        )
    }
}
