package io.legado.app.ui.book.read

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.viewbinding.ViewBinding
import io.legado.app.R
import io.legado.app.ui.book.read.page.ReadView
import io.legado.app.ui.compose.LegadoComposeTheme

class ReadBookComposeBinding(context: Context) : ViewBinding {

    val composeView = ComposeView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
    }
    val contentRoot = FrameLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
    }
    val readView = ReadView(context, EmptyAttributeSet).apply {
        id = R.id.read_view
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
    }
    val textMenuPosition = View(context).apply {
        id = R.id.text_menu_position
        layoutParams = FrameLayout.LayoutParams(0, 0)
        visibility = View.INVISIBLE
    }
    val cursorLeft = ImageView(context).apply {
        id = R.id.cursor_left
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        )
        contentDescription = context.getString(R.string.select_start)
        setImageResource(R.drawable.ic_cursor_left)
        visibility = View.INVISIBLE
    }
    val cursorRight = ImageView(context).apply {
        id = R.id.cursor_right
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        )
        contentDescription = context.getString(R.string.select_end)
        setImageResource(R.drawable.ic_cursor_right)
        visibility = View.INVISIBLE
    }
    val readMenu = ReadMenu(context).apply {
        id = R.id.read_menu
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        visibility = View.GONE
    }
    val searchMenu = SearchMenu(context).apply {
        id = R.id.search_menu
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
        )
        visibility = View.GONE
    }
    val navigationBar = View(context).apply {
        id = R.id.navigation_bar
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            0,
        ).apply {
            gravity = android.view.Gravity.BOTTOM
        }
        setBackgroundResource(R.color.background_menu)
    }

    init {
        contentRoot.addView(readView)
        contentRoot.addView(textMenuPosition)
        contentRoot.addView(cursorLeft)
        contentRoot.addView(cursorRight)
        contentRoot.addView(readMenu)
        contentRoot.addView(searchMenu)
        contentRoot.addView(navigationBar)
    }

    override fun getRoot(): View = composeView
}

private object EmptyAttributeSet : AttributeSet {
    override fun getAttributeCount(): Int = 0
    override fun getAttributeName(index: Int): String = ""
    override fun getAttributeValue(index: Int): String? = null
    override fun getAttributeValue(namespace: String?, name: String?): String? = null
    override fun getPositionDescription(): String = ""
    override fun getAttributeNameResource(index: Int): Int = 0
    override fun getAttributeListValue(
        namespace: String?,
        attribute: String?,
        options: Array<out String>?,
        defaultValue: Int,
    ): Int = defaultValue
    override fun getAttributeBooleanValue(
        namespace: String?,
        attribute: String?,
        defaultValue: Boolean,
    ): Boolean = defaultValue
    override fun getAttributeResourceValue(
        namespace: String?,
        attribute: String?,
        defaultValue: Int,
    ): Int = defaultValue
    override fun getAttributeIntValue(
        namespace: String?,
        attribute: String?,
        defaultValue: Int,
    ): Int = defaultValue
    override fun getAttributeUnsignedIntValue(
        namespace: String?,
        attribute: String?,
        defaultValue: Int,
    ): Int = defaultValue
    override fun getAttributeFloatValue(
        namespace: String?,
        attribute: String?,
        defaultValue: Float,
    ): Float = defaultValue
    override fun getAttributeListValue(
        index: Int,
        options: Array<out String>?,
        defaultValue: Int,
    ): Int = defaultValue
    override fun getAttributeBooleanValue(index: Int, defaultValue: Boolean): Boolean =
        defaultValue
    override fun getAttributeResourceValue(index: Int, defaultValue: Int): Int = defaultValue
    override fun getAttributeIntValue(index: Int, defaultValue: Int): Int = defaultValue
    override fun getAttributeUnsignedIntValue(index: Int, defaultValue: Int): Int = defaultValue
    override fun getAttributeFloatValue(index: Int, defaultValue: Float): Float = defaultValue
    override fun getIdAttribute(): String? = null
    override fun getClassAttribute(): String? = null
    override fun getIdAttributeResourceValue(defaultValue: Int): Int = defaultValue
    override fun getStyleAttribute(): Int = 0
}

@Composable
fun ReadBookScreen(
    contentRoot: FrameLayout,
) {
    LegadoComposeTheme {
        AndroidView(
            factory = { contentRoot },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
