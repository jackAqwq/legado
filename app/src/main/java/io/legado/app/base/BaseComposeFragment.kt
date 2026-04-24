package io.legado.app.base

import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import io.legado.app.utils.applyTint

abstract class BaseComposeFragment : Fragment() {

    var supportToolbar: Toolbar? = null
        private set

    val menuInflater: MenuInflater
        get() = SupportMenuInflater(requireContext())

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Content()
            }
        }
    }

    @Composable
    abstract fun Content()

    override fun onViewCreated(view: View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onMultiWindowModeChanged()
        observeLiveBus()
        onFragmentCreated(view, savedInstanceState)
    }

    open fun onFragmentCreated(view: View, savedInstanceState: android.os.Bundle?) {
    }

    private fun onMultiWindowModeChanged() {
        (activity as? BaseActivity<*>)?.let {
            view?.findViewById<io.legado.app.ui.widget.TitleBar>(io.legado.app.R.id.title_bar)
                ?.onMultiWindowModeChanged(it.isInMultiWindow, it.fullScreen)
        }
    }

    open fun observeLiveBus() {
    }

    open fun onCompatCreateOptionsMenu(menu: Menu) {
    }

    open fun onCompatOptionsItemSelected(item: android.view.MenuItem) {
    }

    fun setSupportToolbar(toolbar: Toolbar) {
        supportToolbar = toolbar
        supportToolbar?.let {
            it.menu.apply {
                onCompatCreateOptionsMenu(this)
                applyTint(requireContext())
            }

            it.setOnMenuItemClickListener {
                onCompatOptionsItemSelected(it)
                true
            }
        }
    }

}
