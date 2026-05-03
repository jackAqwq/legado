package io.legado.app.ui.minireader

import androidx.activity.result.ActivityResultLauncher
import io.legado.app.ui.file.HandleFileContract
import io.legado.app.utils.launch

class MiniReaderFilePickerGateway(
    private val launcher: ActivityResultLauncher<(HandleFileContract.HandleFileParam.() -> Unit)?>
) {

    fun pickTxt() {
        launcher.launch {
            mode = HandleFileContract.FILE
            allowExtensions = arrayOf("txt")
        }
    }
}
