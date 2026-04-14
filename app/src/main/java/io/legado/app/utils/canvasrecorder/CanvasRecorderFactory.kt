package io.legado.app.utils.canvasrecorder

import io.legado.app.help.config.AppConfig

object CanvasRecorderFactory {

    val isSupport = true

    // issue 3868
    fun create(locked: Boolean = false): CanvasRecorder {
        val impl = when {
            !AppConfig.optimizeRender -> CanvasRecorderImpl()
            else -> CanvasRecorderApi29Impl()
        }
        return if (locked) {
            CanvasRecorderLocked(impl)
        } else {
            impl
        }
    }

}
