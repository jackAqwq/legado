package io.legado.app.manifest

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AndroidManifestSoftInputModeTest {

    @Test
    fun keyInputActivitiesUseAdjustResize() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertActivityHasAdjustResize(manifest, ".ui.minireader.MiniReaderActivity")
        assertActivityHasAdjustResize(manifest, ".ui.book.info.edit.BookInfoEditActivity")
        assertActivityHasAdjustResize(manifest, ".ui.book.search.SearchActivity")
        assertActivityHasAdjustResize(manifest, ".ui.book.searchContent.SearchContentActivity")
    }

    private fun assertActivityHasAdjustResize(manifest: String, activityName: String) {
        val activityStart = manifest.indexOf("android:name=\"$activityName\"")
        assertTrue("Missing activity declaration for $activityName", activityStart >= 0)
        val activityEnd = manifest.indexOf('>', activityStart)
        assertTrue("Malformed activity declaration for $activityName", activityEnd > activityStart)
        val declaration = manifest.substring(activityStart, activityEnd)
        assertTrue(
            "Activity $activityName must declare adjustResize",
            declaration.contains("android:windowSoftInputMode=\"adjustResize")
        )
    }
}

