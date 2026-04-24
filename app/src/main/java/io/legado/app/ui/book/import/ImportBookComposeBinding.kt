package io.legado.app.ui.book.import

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.viewbinding.ViewBinding
import io.legado.app.R
import io.legado.app.lib.theme.secondaryTextColor
import io.legado.app.ui.compose.LegadoComposeTheme
import io.legado.app.ui.widget.SelectActionBar
import io.legado.app.ui.widget.TitleBar
import io.legado.app.ui.widget.anima.RefreshProgressBar
import io.legado.app.ui.widget.recycler.scroller.FastScrollRecyclerView
import io.legado.app.ui.widget.text.StrokeTextView
import io.legado.app.utils.dpToPx

class ImportBookComposeBinding(context: Context) : ViewBinding {

    val composeView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    }
    val titleBar = TitleBar(context).apply {
        id = R.id.title_bar
        toolbar.contentInsetStartWithNavigation = 0
        LayoutInflater.from(context).inflate(R.layout.view_search, toolbar, true)
    }
    val layTop = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        minimumHeight = 36.dpToPx()
        setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
        setBackgroundResource(R.color.background)
        elevation = 1.dpToPx().toFloat()
    }
    val tvPath = TextView(context).apply {
        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        gravity = Gravity.CENTER_VERTICAL
        isSingleLine = true
        isFocusable = true
        setTextColor(context.secondaryTextColor)
        textSize = 13f
    }
    val tvGoBack = StrokeTextView(context, null).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val margin = 4.dpToPx()
            setMargins(margin, margin, margin, margin)
        }
        gravity = Gravity.CENTER
        setPadding(16.dpToPx(), 0, 16.dpToPx(), 0)
        setText(R.string.go_back)
        setTypeface(typeface, Typeface.BOLD)
        textSize = 14f
        setRadius(5)
    }
    val refreshProgressBar = RefreshProgressBar(context)
    val recyclerView = FastScrollRecyclerView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
    }
    val tvEmptyMsg = TextView(context).apply {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ).apply {
            val margin = 16.dpToPx()
            setMargins(margin, margin, margin, margin)
        }
        gravity = Gravity.CENTER
        setText(R.string.empty)
        visibility = View.GONE
    }
    val contentRoot = FrameLayout(context).apply {
        addView(recyclerView)
        addView(tvEmptyMsg)
    }
    val selectActionBar = SelectActionBar(context)

    init {
        layTop.addView(tvPath)
        layTop.addView(tvGoBack)
        composeView.setContent {
            ImportBookScreen(this@ImportBookComposeBinding)
        }
    }

    override fun getRoot(): View = composeView
}

@Composable
private fun ImportBookScreen(
    binding: ImportBookComposeBinding,
) {
    LegadoComposeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { binding.titleBar },
                    modifier = Modifier.fillMaxWidth(),
                )
                AndroidView(
                    factory = { binding.layTop },
                    modifier = Modifier.fillMaxWidth(),
                )
                AndroidView(
                    factory = { binding.refreshProgressBar },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                )
                AndroidView(
                    factory = { binding.contentRoot },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                AndroidView(
                    factory = { binding.selectActionBar },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
