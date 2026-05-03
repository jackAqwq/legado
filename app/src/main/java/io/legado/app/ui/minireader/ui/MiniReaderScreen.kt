package io.legado.app.ui.minireader.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import io.legado.app.ui.minireader.MiniReaderProgressManager
import io.legado.app.ui.minireader.MiniReaderViewModel
import io.legado.app.ui.minireader.paging.MiniPageSnapshot
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun MiniReaderScreen(
    state: MiniReaderViewModel.MiniReaderState,
    onTurnPrev: () -> Unit,
    onTurnNext: () -> Unit,
    onAnimationFinished: () -> Unit,
    onSettingsChanged: (MiniReaderProgressManager.MiniReaderSettings) -> Unit,
    onJumpToChapter: (Int) -> Unit,
    onJumpToProgress: (Int) -> Unit,
    onOpenPicker: () -> Unit
) {
    if (state is MiniReaderViewModel.MiniReaderState.Turning) {
        LaunchedEffect(state.from.snapshot.current.index, state.target.snapshot.current.index) {
            delay(160)
            onAnimationFinished()
        }
    }
    when (state) {
        MiniReaderViewModel.MiniReaderState.Idle -> {
            MiniBookshelfScreen(onOpenPicker = onOpenPicker)
        }

        MiniReaderViewModel.MiniReaderState.Loading -> {
            TextPanelScreen("Loading...")
        }

        is MiniReaderViewModel.MiniReaderState.Unavailable -> {
            MiniBookshelfScreen(
                title = "Source unavailable",
                subtitle = "Please pick the TXT file again.",
                onOpenPicker = onOpenPicker
            )
        }

        is MiniReaderViewModel.MiniReaderState.EncodingUnsupported -> {
            MiniBookshelfScreen(
                title = "Encoding unsupported",
                subtitle = state.message,
                onOpenPicker = onOpenPicker
            )
        }

        is MiniReaderViewModel.MiniReaderState.Ready -> {
            ReaderContentScreen(
                snapshotCurrent = state.snapshot.current,
                incoming = null,
                isTurning = false,
                settings = state.settings,
                onTurnPrev = onTurnPrev,
                onTurnNext = onTurnNext,
                onSettingsChanged = onSettingsChanged,
                onJumpToChapter = onJumpToChapter,
                onJumpToProgress = onJumpToProgress
            )
        }

        is MiniReaderViewModel.MiniReaderState.Turning -> {
            ReaderContentScreen(
                snapshotCurrent = state.from.snapshot.current,
                incoming = state.target.snapshot.current,
                isTurning = true,
                settings = state.target.settings,
                onTurnPrev = onTurnPrev,
                onTurnNext = onTurnNext,
                onSettingsChanged = onSettingsChanged,
                onJumpToChapter = onJumpToChapter,
                onJumpToProgress = onJumpToProgress
            )
        }
    }
}

@Composable
private fun ReaderContentScreen(
    snapshotCurrent: MiniPageSnapshot,
    incoming: MiniPageSnapshot?,
    isTurning: Boolean,
    settings: MiniReaderProgressManager.MiniReaderSettings,
    onTurnPrev: () -> Unit,
    onTurnNext: () -> Unit,
    onSettingsChanged: (MiniReaderProgressManager.MiniReaderSettings) -> Unit,
    onJumpToChapter: (Int) -> Unit,
    onJumpToProgress: (Int) -> Unit
) {
    AndroidView(
        factory = { context ->
            LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.parseColor("#F7F3E8"))

                val pageContainer = FrameLayout(context).apply {
                    tag = TAG_PAGE_CONTAINER
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0, 1f)
                }

                val controlArea = ScrollView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    setBackgroundColor(Color.parseColor("#EDE7D9"))
                    addView(
                        buildControls(context, onTurnPrev, onTurnNext, onSettingsChanged, onJumpToChapter, onJumpToProgress)
                    )
                }

                addView(pageContainer)
                addView(controlArea)
            }
        },
        update = { root ->
            val pageContainer = root.findViewWithTag<FrameLayout>(TAG_PAGE_CONTAINER)
            val overlayHost = pageContainer.findViewWithTag<ComposeView>(TAG_OVERLAY_VIEW)
                ?: ComposeView(root.context).apply {
                    tag = TAG_OVERLAY_VIEW
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    pageContainer.addView(this)
                }
            overlayHost.setContent {
                MiniOverlayPageTurnCanvas(
                    current = snapshotCurrent,
                    incoming = incoming,
                    animProgress = if (isTurning) 0.92f else 1f
                )
            }
            root.findViewWithTag<TextView>(TAG_SETTINGS_TEXT).text = renderSettingsSummary(settings)
        }
    )
}

@Composable
private fun TextPanelScreen(text: String) {
    AndroidView(
        factory = { context ->
            TextView(context).apply {
                setBackgroundColor(Color.parseColor("#F4F1E8"))
                setTextColor(Color.parseColor("#333333"))
                gravity = Gravity.CENTER
                textSize = 18f
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
        },
        update = {
            it.text = text
        }
    )
}

private fun buildControls(
    context: Context,
    onTurnPrev: () -> Unit,
    onTurnNext: () -> Unit,
    onSettingsChanged: (MiniReaderProgressManager.MiniReaderSettings) -> Unit,
    onJumpToChapter: (Int) -> Unit,
    onJumpToProgress: (Int) -> Unit
): LinearLayout {
    val controls = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(24, 16, 24, 24)
    }

    val settingsText = TextView(context).apply {
        tag = TAG_SETTINGS_TEXT
        setTextColor(Color.parseColor("#3A332B"))
        textSize = 13f
    }
    controls.addView(settingsText, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

    controls.addView(
        rowOfButtons(
            context,
            "Prev" to onTurnPrev,
            "Next" to onTurnNext
        ),
        FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    )

    controls.addView(
        rowOfButtons(
            context,
            "Font+" to {
                onSettingsChanged(
                    MiniReaderProgressManager.MiniReaderSettings(
                        fontSizeSp = 20,
                        lineSpacingMultiplier = 1.35f,
                        bgMode = MiniReaderProgressManager.BG_MODE_LIGHT,
                        brightness = 100
                    )
                )
            },
            "Spacing+" to {
                onSettingsChanged(
                    MiniReaderProgressManager.MiniReaderSettings(
                        fontSizeSp = 18,
                        lineSpacingMultiplier = 1.6f,
                        bgMode = MiniReaderProgressManager.BG_MODE_LIGHT,
                        brightness = 100
                    )
                )
            },
            "EyeCare" to {
                onSettingsChanged(
                    MiniReaderProgressManager.MiniReaderSettings(
                        fontSizeSp = 18,
                        lineSpacingMultiplier = 1.35f,
                        bgMode = MiniReaderProgressManager.BG_MODE_EYE_CARE,
                        brightness = 90
                    )
                )
            },
            "Bright+" to {
                onSettingsChanged(
                    MiniReaderProgressManager.MiniReaderSettings(
                        fontSizeSp = 18,
                        lineSpacingMultiplier = 1.35f,
                        bgMode = MiniReaderProgressManager.BG_MODE_LIGHT,
                        brightness = 100
                    )
                )
            }
        ),
        FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    )

    controls.addView(
        rowOfButtons(
            context,
            "Chapter 1" to { onJumpToChapter(0) },
            "Progress 50%" to { onJumpToProgress(50) }
        ),
        FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    )

    return controls
}

private fun rowOfButtons(
    context: Context,
    vararg actions: Pair<String, () -> Unit>
): LinearLayout {
    return LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(0, 12, 0, 0)
        actions.forEach { (label, action) ->
            val button = Button(context).apply {
                text = label
                isAllCaps = false
                setOnClickListener { action() }
            }
            val params = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).apply {
                marginEnd = 8
            }
            addView(button, params)
        }
    }
}

private fun renderSettingsSummary(settings: MiniReaderProgressManager.MiniReaderSettings): String {
    val spacing = String.format(Locale.US, "%.2f", settings.lineSpacingMultiplier)
    return "Font ${settings.fontSizeSp}sp | Spacing $spacing | BG ${settings.bgMode} | Light ${settings.brightness}%"
}

private const val TAG_PAGE_CONTAINER = "mini_reader_page_container"
private const val TAG_OVERLAY_VIEW = "mini_reader_overlay_compose_view"
private const val TAG_SETTINGS_TEXT = "mini_reader_settings_text"
