package io.legado.app.ui.minireader

import android.os.Bundle
import androidx.activity.viewModels
import io.legado.app.base.BaseViewModel
import io.legado.app.base.VMBaseActivity
import io.legado.app.databinding.ActivityTranslucenceBinding
import io.legado.app.utils.viewbindingdelegate.viewBinding

class MiniReaderActivity : VMBaseActivity<ActivityTranslucenceBinding, BaseViewModel>() {

    override val binding by viewBinding(ActivityTranslucenceBinding::inflate)
    override val viewModel by viewModels<BaseViewModel>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        // Minimal shell to keep mini-reader intent route safe before formal UI lands.
    }
}
