package io.legado.app.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MenuAndToolbarReflectionSourceGuardTest {

    @Test
    fun menuExtensionsAvoidsMenuReflectionPath() {
        val source = File("src/main/java/io/legado/app/utils/MenuExtensions.kt").readText()
        assertFalse(
            "MenuBuilder reflection should not be used",
            source.contains("getDeclaredMethod(\"setOptionalIconsVisible\"")
        )
        assertFalse(
            "getNonActionItems reflection should not be used",
            source.contains("getDeclaredMethod(\"getNonActionItems\"")
        )
        assertFalse(
            "Menu class-name string checks should not be used",
            source.contains("javaClass.simpleName.equals(\"MenuBuilder\"")
        )
        assertFalse(
            "SubMenu class-name string checks should not be used",
            source.contains("javaClass.simpleName.equals(\"SubMenuBuilder\"")
        )
        assertTrue(
            "MenuBuilder typed path should remain in place",
            source.contains("if (this is MenuBuilder)")
        )
    }

    @Test
    fun changeSourceDialogAvoidsToolbarFieldReflection() {
        val source =
            File("src/main/java/io/legado/app/ui/book/changesource/ChangeBookSourceDialog.kt")
                .readText()
        assertFalse(
            "Toolbar mNavButtonView reflection should not be used",
            source.contains("getDeclaredField(\"mNavButtonView\"")
        )
        assertTrue(
            "Navigation icon should be tinted directly",
            source.contains("navigationIcon?.setTintMutate(textColor)")
        )
    }
}
