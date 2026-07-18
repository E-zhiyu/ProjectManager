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

-keep class com.manager.assistant.data.io.pojos.** { *; }       # 保护POJO类
-keep class com.manager.assistant.data.io.maps.** { *; }        # 保护POJO类的集合
-keep class com.manager.assistant.data.save.database.** { *; }  # 保护数据库相关的类

# 保护流水记录类型枚举，因为使用了valueOf()方法
-keep enum com.manager.assistant.auxiliary.enums.AccountType { *; }

# 保护版本更新帮助类
-keep class com.manager.assistant.helpers.** { *; }

# 保护预算重置频率枚举
-keep enum com.manager.assistant.ui.pages.budget.ResetFrequency { *; }