package io.legado.app.utils.canvasrecorder.pools

import android.graphics.RenderNode
import io.legado.app.utils.objectpool.BaseObjectPool

class RenderNodePool : BaseObjectPool<RenderNode>(64) {

    override fun recycle(target: RenderNode) {
        target.discardDisplayList()
        super.recycle(target)
    }

    override fun create(): RenderNode = RenderNode("CanvasRecorder")

}
