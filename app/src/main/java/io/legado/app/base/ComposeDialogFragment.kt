package io.legado.app.base

import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import io.legado.app.R
import io.legado.app.constant.AppLog
import io.legado.app.help.config.AppConfig
import io.legado.app.help.coroutine.Coroutine
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.utils.dpToPx
import io.legado.app.utils.setBackgroundKeepPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

abstract class ComposeDialogFragment(
    private val adaptationSoftKeyboard: Boolean = false
) : DialogFragment() {

    private var onDismissListener: OnDismissListener? = null

    fun setOnDismissListener(onDismissListener: OnDismissListener?) {
        this.onDismissListener = onDismissListener
    }

    @Composable
    protected abstract fun DialogContent()

    open fun onDialogCreated(savedInstanceState: Bundle?) {
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DialogContent()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (adaptationSoftKeyboard) {
            dialog?.window?.setBackgroundDrawableResource(R.color.transparent)
        } else if (AppConfig.isEInkMode) {
            dialog?.window?.let {
                it.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                val attr = it.attributes
                attr.dimAmount = 0.0f
                attr.windowAnimations = 0
                it.attributes = attr
                it.decorView.setBackgroundKeepPadding(R.color.transparent)
            }
            lifecycle.addObserver(LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    when (dialog?.window?.attributes?.gravity) {
                        Gravity.TOP -> view?.setBackgroundResource(R.drawable.bg_eink_border_bottom)
                        Gravity.BOTTOM -> view?.setBackgroundResource(R.drawable.bg_eink_border_top)
                        else -> {
                            val padding = 2.dpToPx()
                            view?.setPadding(padding, padding, padding, padding)
                            view?.setBackgroundResource(R.drawable.bg_eink_border_dialog)
                        }
                    }
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!adaptationSoftKeyboard && !AppConfig.isEInkMode) {
            view.setBackgroundColor(ThemeStore.backgroundColor())
        }
        onDialogCreated(savedInstanceState)
        observeLiveBus()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        kotlin.runCatching {
            manager.beginTransaction().remove(this).commit()
            super.show(manager, tag)
        }.onFailure {
            AppLog.put("显示 Compose 对话框失败 tag:$tag", it)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss(dialog)
    }

    fun <T> execute(
        scope: CoroutineScope = lifecycleScope,
        context: CoroutineContext = Dispatchers.IO,
        block: suspend CoroutineScope.() -> T
    ) = Coroutine.async(scope, context) { block() }

    open fun observeLiveBus() {
    }
}
