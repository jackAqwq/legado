package io.legado.app.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView

abstract class BaseComposeActivity : AppCompatActivity() {

    protected val composeView by lazy {
        ComposeView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(composeView)
        composeView.setContent {
            Content()
        }
    }

    @Composable
    abstract fun Content()
}
