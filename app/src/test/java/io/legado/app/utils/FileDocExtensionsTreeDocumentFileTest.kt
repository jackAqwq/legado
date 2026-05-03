package io.legado.app.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FileDocExtensionsTreeDocumentFileTest {

    @Test
    fun usesPublicTreeUriApiInsteadOfReflection() {
        val source = File("src/main/java/io/legado/app/utils/FileDocExtensions.kt").readText()
        assertFalse(
            "TreeDocumentFile reflection should not be used",
            source.contains("Class.forName(\"androidx.documentfile.provider.TreeDocumentFile\")")
        )
        assertTrue(
            "Tree directories should use the public fromTreeUri API",
            source.contains("DocumentFile.fromTreeUri(appCtx, uri)")
        )
    }
}
