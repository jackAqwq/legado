package io.legado.app.help

import io.legado.app.help.config.AppConfig
import io.legado.app.utils.LogUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

object DispatchersMonitor {

    private const val TAG = "DispatchersMonitor"

    private var dispatcher = Executors.newSingleThreadExecutor { 
        Thread(it, TAG) 
    }.asCoroutineDispatcher()

    private var scope = CoroutineScope(dispatcher)

    fun init() {
        scope.coroutineContext.cancelChildren()
        if (!AppConfig.recordLog) {
            return
        }
        monitor(IO)
        monitor(Default)
        monitor(Main)
    }

    /**
     * 清理资源
     */
    fun clear() {
        scope.coroutineContext.cancelChildren()
        dispatcher.close()
        // 重新创建dispatcher和scope，以便下次初始化时使用
        dispatcher = Executors.newSingleThreadExecutor { 
            Thread(it, TAG) 
        }.asCoroutineDispatcher()
        scope = CoroutineScope(dispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun monitor(dispatcher: CoroutineDispatcher) {
        scope.launch {
            while (isActive) select {
                launch {
                    withContext(dispatcher) {
                        delay(3000)
                    }
                }.onJoin {}
                onTimeout(5000) {
                    LogUtils.d(TAG, "Dispatcher $dispatcher is timed out waiting for for 5000ms.")
                }
            }
        }
    }

}
