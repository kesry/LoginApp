# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# 保留项目主包下的所有类和成员
-keep class eg.kesry.loginApp.** { *; }

# 保留Activity类不被混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Application

# 保留自定义View类
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留Parcelable实现类
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# 保留注解类
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# 保留行号信息用于调试
-keepattributes SourceFile,LineNumberTable

# 保留反射使用的类和方法
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Jackson JSON库保护规则
-keep class com.fasterxml.jackson.databind.ObjectMapper {
    public <methods>;
    protected <methods>;
}
-keep class com.fasterxml.jackson.databind.JsonMappingException {
    <init>(java.lang.String);
}
-keep class com.fasterxml.jackson.databind.JsonNode {
    <init>(java.lang.String);
}
-keep class com.fasterxml.jackson.databind.node.* { *; }
-keep class com.fasterxml.jackson.annotation.** { *; }

# OkHttp3保护规则
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# ZXing二维码库保护规则
-keep class com.google.zxing.** { *; }
-keep interface com.google.zxing.** { *; }
-keep class com.journeyapps.barcodescanner.** { *; }
-keep interface com.journeyapps.barcodescanner.** { *; }

# AndroidX相关保护规则
-keep class androidx.** { *; }
-keep interface androidx.** { *; }

# Google Material Design组件保护
-keep class com.google.android.material.** { *; }
-keep interface com.google.android.material.** { *; }

# 保留资源文件引用
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 保留native方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留序列化类
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 针对特定业务类的保护规则
# 保留登录相关的回调接口
-keep interface eg.kesry.loginApp.callback.** { *; }
-keep class eg.kesry.loginApp.callback.** { *; }

# 保留Bean类（数据模型）
-keep class eg.kesry.loginApp.bean.** { *; }

# 保留监听器接口
-keep interface eg.kesry.loginApp.listener.** { *; }

# 保留工具类
-keep class eg.kesry.loginApp.utils.** { *; }

# 保留适配器类
-keep class eg.kesry.loginApp.adapter.** { *; }

# 如果你的项目使用了WebView，取消下面的注释
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# 如果你想隐藏原始源文件名，取消下面的注释
#-renamesourcefileattribute SourceFile