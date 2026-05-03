package io.legado.app.ui.minireader.ui

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import io.legado.app.ui.minireader.paging.MiniPageSnapshot
import kotlin.math.abs

@Composable
fun MiniOverlayPageTurnCanvas(
    current: MiniPageSnapshot,
    incoming: MiniPageSnapshot?,
    animProgress: Float
) {
    AndroidView(
        factory = { context ->
            FrameLayout(context).apply {
                val currentTextView = TextView(context).apply {
                    tag = TAG_CURRENT
                    setTextColor(Color.BLACK)
                    setPadding(32, 32, 32, 32)
                    gravity = Gravity.TOP or Gravity.START
                    textSize = 18f
                    setBackgroundColor(Color.TRANSPARENT)
                }
                val incomingTextView = TextView(context).apply {
                    tag = TAG_INCOMING
                    setTextColor(Color.BLACK)
                    setPadding(32, 32, 32, 32)
                    gravity = Gravity.TOP or Gravity.START
                    textSize = 18f
                    alpha = 0f
                    setBackgroundColor(Color.TRANSPARENT)
                }
                addView(
                    currentTextView,
                    FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                )
                addView(
                    incomingTextView,
                    FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                )
            }
        },
        update = { root ->
            val currentTextView = root.findViewWithTag<TextView>(TAG_CURRENT)
            val incomingTextView = root.findViewWithTag<TextView>(TAG_INCOMING)
            currentTextView.text = renderPageText(current)

            if (incoming != null) {
                incomingTextView.text = renderPageText(incoming)
                incomingTextView.alpha = animProgress.coerceIn(0f, 1f)
                val width = root.width.takeIf { it > 0 } ?: root.measuredWidth
                val direction = if (incoming.index >= current.index) 1 else -1
                val slideOffset = ((1f - animProgress.coerceIn(0f, 1f)) * width)
                incomingTextView.translationX = direction * slideOffset
                currentTextView.translationX = -direction * abs(slideOffset) * 0.08f
            } else {
                incomingTextView.text = ""
                incomingTextView.alpha = 0f
                incomingTextView.translationX = 0f
                currentTextView.translationX = 0f
            }
        }
    )
}

private fun renderPageText(page: MiniPageSnapshot): String {
    return buildString {
        append(page.title)
        append('\n')
        append('\n')
        append(page.text)
    }
}

private const val TAG_CURRENT = "mini_reader_overlay_current"
private const val TAG_INCOMING = "mini_reader_overlay_incoming"
