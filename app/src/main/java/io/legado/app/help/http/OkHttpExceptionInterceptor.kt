package io.legado.app.help.http

import io.legado.app.constant.AppLog
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

object OkHttpExceptionInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        
        try {
            return chain.proceed(request)
        } catch (e: IOException) {
            // 记录网络异常
            AppLog.put("网络请求异常: $url", e)
            // 可以根据不同的异常类型提供更具体的错误信息
            when (e) {
                is java.net.SocketTimeoutException -> {
                    throw IOException("网络连接超时，请检查网络状态", e)
                }
                is java.net.ConnectException -> {
                    throw IOException("无法连接到服务器，请检查网络状态", e)
                }
                is java.net.UnknownHostException -> {
                    throw IOException("无法解析服务器地址，请检查网络状态", e)
                }
                else -> {
                    throw e
                }
            }
        } catch (e: Throwable) {
            // 记录其他异常
            AppLog.put("网络请求未知异常: $url", e)
            throw IOException("网络请求失败，请稍后重试", e)
        }
    }

}
