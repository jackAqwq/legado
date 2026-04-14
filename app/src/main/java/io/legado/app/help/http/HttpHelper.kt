package io.legado.app.help.http

import io.legado.app.App
import io.legado.app.constant.AppConst
import io.legado.app.help.CacheManager
import io.legado.app.help.config.AppConfig
import io.legado.app.help.glide.progress.ProgressManager.LISTENER
import io.legado.app.help.glide.progress.ProgressResponseBody
import io.legado.app.help.http.CookieManager.cookieJarHeader
import io.legado.app.model.ReadManga
import io.legado.app.utils.NetworkUtils
import okhttp3.Cache
import okhttp3.ConnectionSpec
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Credentials
import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 代理客户端缓存，用于缓存不同代理配置的OkHttpClient实例
 */
private val proxyClientCache: ConcurrentHashMap<String, OkHttpClient> by lazy {
    ConcurrentHashMap()
}

/**
 * OkHttp缓存目录
 */
private val cacheDir by lazy {
    File(App.INSTANCE.cacheDir, "okhttp_cache")
}

/**
 * OkHttp缓存对象，大小为10MB
 */
private val cache by lazy {
    Cache(cacheDir, 10 * 1024 * 1024) // 10MB缓存
}

/**
 * 缓存拦截器，处理网络请求的缓存策略
 */
private val cacheInterceptor = Interceptor {
    val request = it.request()
    val response = it.proceed(request)
    
    // 缓存策略：在线时缓存5分钟，离线时缓存7天
    val cacheControl = if (NetworkUtils.isNetworkAvailable(App.INSTANCE)) {
        "public, max-age=300" // 在线时缓存5分钟
    } else {
        "public, only-if-cached, max-stale=604800" // 离线时缓存7天
    }
    
    // 构建响应，设置缓存控制头
    response.newBuilder()
        .header("Cache-Control", cacheControl)
        .removeHeader("Pragma") // 移除Pragma头，避免与Cache-Control冲突
        .build()
}

/**
 * Cookie管理器，用于临时保存Cookie
 */
val cookieJar by lazy {
    object : CookieJar {
        /**
         * 加载请求的Cookie
         */
        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return emptyList() // 暂时返回空列表，实际使用时会通过CookieManager加载
        }

        /**
         * 保存响应的Cookie
         */
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            if (cookies.isEmpty()) return
            
            // 临时保存Cookie，书源启用cookie选项时再添加到数据库
            val cookieBuilder = StringBuilder()
            cookies.forEachIndexed { index, cookie ->
                if (index > 0) cookieBuilder.append(";")
                cookieBuilder.append(cookie.name).append('=').append(cookie.value)
            }
            val domain = NetworkUtils.getSubDomain(url.toString())
            CacheManager.putMemory("${domain}_cookieJar", cookieBuilder.toString())
        }
    }
}

/**
 * 全局OkHttpClient实例
 */
val okHttpClient: OkHttpClient by lazy {
    // 连接规格配置
    val specs = arrayListOf(
        ConnectionSpec.MODERN_TLS,  // 现代TLS配置
        ConnectionSpec.COMPATIBLE_TLS,  // 兼容TLS配置
        ConnectionSpec.CLEARTEXT  // 明文连接配置
    )

    val builder = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)  // 连接超时时间
        .writeTimeout(15, TimeUnit.SECONDS)  // 写入超时时间
        .readTimeout(60, TimeUnit.SECONDS)  // 读取超时时间
        .callTimeout(60, TimeUnit.SECONDS)  // 调用超时时间
        //.cookieJar(cookieJar = cookieJar)  // Cookie管理器（暂时注释）
        .sslSocketFactory(SSLHelper.unsafeSSLSocketFactory, SSLHelper.unsafeTrustManager)  // 不安全的SSL配置
        .retryOnConnectionFailure(true)  // 连接失败时重试
        .hostnameVerifier(SSLHelper.unsafeHostnameVerifier)  // 不安全的主机名验证
        .connectionSpecs(specs)  // 连接规格
        .followRedirects(true)  // 跟随重定向
        .followSslRedirects(true)  // 跟随SSL重定向
        .cache(cache)  // 缓存
        .addInterceptor(OkHttpExceptionInterceptor)  // 异常处理拦截器
        .addInterceptor(cacheInterceptor)  // 缓存拦截器
        .addInterceptor { chain ->
            // 添加通用请求头
            val request = chain.request()
            val builder = request.newBuilder()
            
            // 设置User-Agent
            if (request.header(AppConst.UA_NAME) == null) {
                builder.addHeader(AppConst.UA_NAME, AppConfig.userAgent)
            } else if (request.header(AppConst.UA_NAME) == "null") {
                builder.removeHeader(AppConst.UA_NAME)
            }
            
            // 添加Keep-Alive头
            builder.addHeader("Keep-Alive", "300")
            builder.addHeader("Connection", "Keep-Alive")
            
            chain.proceed(builder.build())
        }
        .addNetworkInterceptor { chain ->
            // 处理Cookie
            var request = chain.request()
            val enableCookieJar = request.header(cookieJarHeader) != null

            if (enableCookieJar) {
                val requestBuilder = request.newBuilder()
                requestBuilder.removeHeader(cookieJarHeader)
                request = CookieManager.loadRequest(requestBuilder.build())
            }

            val networkResponse = chain.proceed(request)

            if (enableCookieJar) {
                CookieManager.saveResponse(networkResponse)
            }
            networkResponse
        }
    
    // 配置DNS缓存
    if (AppConfig.addressCache.isNotEmpty()) {
        builder.dns { hostname ->
            val cachedAddress = AppConfig.addressCache[hostname]
            cachedAddress ?: Dns.SYSTEM.lookup(hostname)
        }
    }
    
    // 配置Cronet
    if (AppConfig.isCronet) {
        if (Cronet.loader?.install() == true) {
            Cronet.interceptor?.let {
                builder.addInterceptor(it)
            }
        }
    }
    
    // 添加解压缩拦截器
    builder.addInterceptor(DecompressInterceptor)
    
    // 构建并配置线程池
    builder.build().apply {
        val okHttpName = 
            OkHttpClient::class.java.name.removePrefix("okhttp3.").removeSuffix("Client")
        val executor = dispatcher.executorService as ThreadPoolExecutor
        val threadName = "$okHttpName Dispatcher"
        
        // 配置线程工厂
        executor.threadFactory = ThreadFactory { runnable ->
            Thread(runnable, threadName).apply {
                isDaemon = false
                uncaughtExceptionHandler = OkhttpUncaughtExceptionHandler
            }
        }
    }
}

/**
 * 用于漫画的OkHttpClient实例，添加了进度监听和速率限制
 */
val okHttpClientManga by lazy {
    okHttpClient.newBuilder().run {
        val interceptors = interceptors()
        
        // 添加进度监听拦截器
        interceptors.add(1) { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            val url = request.url.toString()
            response.newBuilder()
                .body(ProgressResponseBody(url, LISTENER, response.body))
                .build()
        }
        
        // 添加速率限制拦截器
        interceptors.add(1) { chain ->
            ReadManga.rateLimiter.withLimitBlocking {
                chain.proceed(chain.request())
            }
        }
        
        build()
    }
}

/**
 * 获取带代理的OkHttpClient实例
 * @param proxy 代理配置字符串，格式为：http://host:port 或 socks5://host:port 或 http://user:pass@host:port
 * @return 配置了代理的OkHttpClient实例
 */
fun getProxyClient(proxy: String? = null): OkHttpClient {
    // 如果没有代理配置，返回默认客户端
    if (proxy.isNullOrBlank()) {
        return okHttpClient
    }
    
    // 从缓存中获取已配置的客户端
    proxyClientCache[proxy]?.let {
        return it
    }
    
    // 解析代理配置
    val r = Regex("(http|socks4|socks5)://(.*):(\\d{2,5})(@.*@.*)?")
    val ms = r.findAll(proxy)
    val group = ms.first()
    
    var username = "" // 代理服务器验证用户名
    var password = "" // 代理服务器验证密码
    val type = if (group.groupValues[1] == "http") "http" else "socks"
    val host = group.groupValues[2]
    val port = group.groupValues[3].toInt()
    
    // 解析用户名和密码
    if (group.groupValues[4] != "") {
        username = group.groupValues[4].split("@")[1]
        password = group.groupValues[4].split("@")[2]
    }
    
    // 构建代理客户端
    if (host != "") {
        val builder = okHttpClient.newBuilder()
        
        // 设置代理
        if (type == "http") {
            builder.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(host, port)))
        } else {
            builder.proxy(Proxy(Proxy.Type.SOCKS, InetSocketAddress(host, port)))
        }
        
        // 设置代理认证
        if (username != "" && password != "") {
            builder.proxyAuthenticator { _, response ->
                val credential: String = Credentials.basic(username, password)
                response.request.newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build()
            }
        }
        
        val proxyClient = builder.build()
        proxyClientCache[proxy] = proxyClient
        return proxyClient
    }
    
    return okHttpClient
}