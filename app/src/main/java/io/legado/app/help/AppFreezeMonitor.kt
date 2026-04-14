package io.legado.app.help

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import io.legado.app.help.config.AppConfig
import io.legado.app.utils.LogUtils

object AppFreezeMonitor {

    private const val TAG = "AppFreezeMonitor"

    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null

    private val screenStatusReceiver by lazy {
        ScreenStatusReceiver()
    }

    private var registeredReceiver = false
    private var context: Context? = null

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    fun init(context: Context) {
        this.context = context.applicationContext
        
        if (!AppConfig.recordLog) {
            unregister()
            return
        }

        if (!registeredReceiver) {
            registeredReceiver = true
            context.registerReceiver(screenStatusReceiver, screenStatusReceiver.filter)
        }

        // 初始化handler
        if (handler == null) {
            handlerThread = HandlerThread("AppFreezeMonitor").apply { start() }
            handler = Handler(handlerThread?.looper)
        }

        var previous = SystemClock.uptimeMillis()

        val runnable = object : Runnable {
            override fun run() {
                val current = SystemClock.uptimeMillis()
                val elapsed = current - previous
                val extra = elapsed - 3000

                if (extra > 300) {
                    LogUtils.d(TAG, "检测到应用被系统冻结，时长：$extra 毫秒")
                }

                previous = current

                if (AppConfig.recordLog && handler != null) {
                    handler?.postDelayed(this, 3000)
                }
            }
        }
        handler?.postDelayed(runnable, 3000)
    }

    /**
     * 取消注册和清理资源
     */
    fun unregister() {
        if (registeredReceiver && context != null) {
            try {
                context?.unregisterReceiver(screenStatusReceiver)
                registeredReceiver = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // 清理handler
        handler?.removeCallbacksAndMessages(null)
        handlerThread?.quitSafely()
        handler = null
        handlerThread = null
    }

    class ScreenStatusReceiver : BroadcastReceiver() {

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON -> LogUtils.d(TAG, "SCREEN_ON")
                Intent.ACTION_SCREEN_OFF -> LogUtils.d(TAG, "SCREEN_OFF")
            }
        }
    }

}
