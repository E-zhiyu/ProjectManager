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

-keep class com.sly.coffer.data.save.** { *; }            # 保护数据保存相关的类
-keep class com.sly.coffer.data.backup.** { *; }          # 保护POJO类
-keep class com.sly.coffer.auxiliary.classes.** { *; }    # 保护辅助类

# 保护更新信息类
-keep class com.sly.coffer.helpers.UpdateHelper$VersionInfo { *; }