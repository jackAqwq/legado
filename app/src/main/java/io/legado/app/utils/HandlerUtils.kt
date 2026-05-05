@file:Suppress("unused")

package io.legado.app.utils

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

/** This main looper cache avoids synchronization overhead when accessed repeatedly. */
private val mainLooper: Looper = Looper.getMainLooper()

private val mainThread: Thread = mainLooper.thread

val isMainThread: Boolean get() = mainThread === Thread.currentThread()

fun buildMainHandler(): Handler {
    return Handler.createAsync(mainLooper)
}

private val mainHandler by lazy { buildMainHandler() }

fun runOnUI(function: () -> Unit) {
    if (isMainThread) {
        function()
    } else {
        mainHandler.post(function)
    }
}

fun CoroutineScope.runOnIO(function: () -> Unit) {
    if (isMainThread) {
        launch(IO) {
            function()
        }
    } else {
        function()
    }
}
