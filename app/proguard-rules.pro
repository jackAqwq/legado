# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# 混合时不使用大小写混合，混合后的类名为小写
-dontusemixedcaseclassnames

# 这句话能够使我们的项目混淆后产生映射文件
# 包含有类名->混淆后类名的映射关系
-verbose

# 保留Annotation不混淆
-keepattributes *Annotation*,InnerClasses

# 避免混淆泛型
-keepattributes Signature

# 指定混淆是采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/cast,!field/*,!class/merging/*

-flattenpackagehierarchy

#############################################
#
# Android开发中一些需要保留的公共部分
#
#############################################
# 屏蔽错误Unresolved class name
#noinspection ShrinkerUnresolvedReference

# 移除Log类打印各个等级日志的代码，打正式包的时候可以做为禁log使用，这里可以作为禁止log打印的功能使用
# 记得proguard-android.txt中一定不要加-dontoptimize才起作用
# 另外的一种实现方案是通过BuildConfig.DEBUG的变量来控制
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# 保持js引擎调用的java类
-keep class * extends io.legado.app.help.JsExtensions{*;}
# 数据类
## NOTE: data.entities keeps explicit model classes because API/import-export/backup JSON
## compatibility relies on stable member names across persisted schemas and JS-bound models.
## Narrowed from package-wide to explicit models; DB-only cache/cookie/projection models are excluded.
-keep class io.legado.app.data.entities.Book { *; }
-keep class io.legado.app.data.entities.Book$ReadConfig { *; }
-keep class io.legado.app.data.entities.BookChapter { *; }
-keep class io.legado.app.data.entities.BookGroup { <fields>; }
-keep class io.legado.app.data.entities.BookSource { *; }
-keep class io.legado.app.data.entities.Bookmark { <fields>; }
-keep class io.legado.app.data.entities.BookProgress { *; }
-keep class io.legado.app.data.entities.DictRule { *; }
-keep class io.legado.app.data.entities.HttpTTS { *; }
-keep class io.legado.app.data.entities.KeyboardAssist { *; }
-keep class io.legado.app.data.entities.ReadRecord { *; }
-keep class io.legado.app.data.entities.ReplaceRule { *; }
-keep class io.legado.app.data.entities.RssSource { *; }
-keep class io.legado.app.data.entities.RssStar { *; }
-keep class io.legado.app.data.entities.SearchBook { *; }
-keep class io.legado.app.data.entities.SearchKeyword { <fields>; }
-keep class io.legado.app.data.entities.Server { *; }
-keep class io.legado.app.data.entities.Server$WebDavConfig { *; }
-keep class io.legado.app.data.entities.TxtTocRule { <fields>; }
## Rule models narrowed from package-wide keep to explicit classes.
-keep class io.legado.app.data.entities.rule.BookInfoRule { *; }
-keep class io.legado.app.data.entities.rule.ContentRule { *; }
-keep class io.legado.app.data.entities.rule.ExploreKind { *; }
-keep class io.legado.app.data.entities.rule.ExploreRule { *; }
-keep class io.legado.app.data.entities.rule.FlexChildStyle { *; }
-keep class io.legado.app.data.entities.rule.ReviewRule { *; }
-keep class io.legado.app.data.entities.rule.RowUi { *; }
-keep class io.legado.app.data.entities.rule.SearchRule { *; }
-keep class io.legado.app.data.entities.rule.TocRule { *; }
# hutool-core hutool-crypto
-keep class
!cn.hutool.core.util.RuntimeUtil,
!cn.hutool.core.util.ClassLoaderUtil,
!cn.hutool.core.util.ReflectUtil,
!cn.hutool.core.util.SerializeUtil,
!cn.hutool.core.util.ClassUtil,
cn.hutool.core.codec.Base64,
cn.hutool.core.codec.PercentCodec,
cn.hutool.core.net.RFC3986,
cn.hutool.core.net.URLDecoder,
cn.hutool.core.net.URLEncodeUtil,
cn.hutool.core.util.HexUtil,
cn.hutool.core.lang.Validator{*;}
-keep class
cn.hutool.crypto.KeyUtil,
cn.hutool.crypto.digest.DigestUtil,
cn.hutool.crypto.digest.Digester,
cn.hutool.crypto.digest.HMac,
cn.hutool.crypto.asymmetric.KeyType,
cn.hutool.crypto.asymmetric.AsymmetricCrypto,
cn.hutool.crypto.asymmetric.Sign,
cn.hutool.crypto.symmetric.AES,
cn.hutool.crypto.symmetric.SymmetricCrypto{*;}
-dontwarn cn.hutool.**

# markwon
-dontwarn org.commonmark.ext.gfm.**

## OkHttp / Okio / JsonPath already provide consumer rules for common runtime paths.
## Keep app-specific reflective entry points only; avoid package-wide keep by default.

# FileDocExtensions.kt now uses public DocumentFile.fromTreeUri()
# JsoupXpath
-keep,allowobfuscation class * implements org.seimicrawler.xpath.core.AxisSelector{*;}
-keep,allowobfuscation class * implements org.seimicrawler.xpath.core.NodeTest{*;}
-keep,allowobfuscation class * implements org.seimicrawler.xpath.core.Function{*;}

## JSOUP
## Jsoup APIs are invoked by runtime JS rules (assets/user scripts) via string-based class/member access.
## Preserve API surface but allow method-level optimization.
-keep,allowoptimization class org.jsoup.** { *; }
-dontwarn org.jspecify.annotations.NullMarked

## ExoPlayer 如果还不能播放就取消注释这个
# -keep class com.google.android.exoplayer2.** {*;}

# Cronet
-keepclassmembers class org.chromium.net.X509Util {
    android.net.http.X509TrustManagerExtensions sDefaultTrustManager;
    android.net.http.X509TrustManagerExtensions sTestTrustManager;
}

# Throwable
-keepnames class * extends java.lang.Throwable

# Sora Editor

# GSYVideoPlayer
-dontwarn com.shuyu.gsyvideoplayer.**
#-keep class com.shuyu.gsyvideoplayer.video.** { *; }
#-dontwarn com.shuyu.gsyvideoplayer.video.**
#-keep class com.shuyu.gsyvideoplayer.video.base.** { *; }
#-dontwarn com.shuyu.gsyvideoplayer.video.base.**
#-keep class com.shuyu.gsyvideoplayer.utils.** { *; }
#-dontwarn com.shuyu.gsyvideoplayer.utils.**
#-keep class com.shuyu.gsyvideoplayer.player.** {*;}
#-dontwarn com.shuyu.gsyvideoplayer.player.**
#-keep class tv.danmaku.ijk.** { *; }
#-dontwarn tv.danmaku.ijk.**
#-keep class androidx.media3.** {*;}
#-keep interface androidx.media3.**
#-keep class com.shuyu.alipay.** {*;}
#-keep interface com.shuyu.alipay.**
## XML-inflated custom views are already covered by AAPT-generated keep rules.
## Do not duplicate app custom-view keeps here.

