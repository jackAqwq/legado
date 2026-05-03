package io.legado.app.ui.minireader.ui

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MiniBookshelfScreen(
    title: String = "Mini reader",
    subtitle: String = "Open local TXT with system picker",
    onOpenPicker: () -> Unit
) {
    AndroidView(
        factory = { context ->
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.parseColor("#F4F1E8"))
                setPadding(32, 48, 32, 48)
                val titleView = TextView(context).apply {
                    tag = TAG_TITLE
                    textSize = 24f
                    setTextColor(Color.parseColor("#222222"))
                    gravity = Gravity.START
                }
                val subtitleView = TextView(context).apply {
                    tag = TAG_SUBTITLE
                    textSize = 15f
                    setTextColor(Color.parseColor("#555555"))
                    setPadding(0, 16, 0, 24)
                }
                val openButton = Button(context).apply {
                    tag = TAG_BUTTON
                    text = "Select TXT"
                    isAllCaps = false
                    setOnClickListener { onOpenPicker() }
                }
                addView(
                    titleView,
                    FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                )
                addView(
                    subtitleView,
                    FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                )
                addView(
                    openButton,
                    FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                )
            }
        },
        update = { root ->
            root.findViewWithTag<TextView>(TAG_TITLE).text = title
            root.findViewWithTag<TextView>(TAG_SUBTITLE).text = subtitle
        }
    )
}

private const val TAG_TITLE = "mini_reader_bookshelf_title"
private const val TAG_SUBTITLE = "mini_reader_bookshelf_subtitle"
private const val TAG_BUTTON = "mini_reader_bookshelf_button"
