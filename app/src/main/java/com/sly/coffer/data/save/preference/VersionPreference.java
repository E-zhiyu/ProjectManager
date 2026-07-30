package com.sly.coffer.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class VersionPreference {
    private static final String PREF_NAME = "VersionPreference";
    private static final String KEY_SKIP_VERSION_CODE = "skip_version_code";    //跳过更新的版本代码
    private static final String KEY_APK_URI = "apk_uri";                        //安装包Uri

    /**
     * 设置跳过的版本代码
     *
     * @param context     上下文
     * @param versionCode 版本代码
     */
    public static void setSkipVersionCode(@NonNull Context context, long versionCode) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putLong(KEY_SKIP_VERSION_CODE, versionCode).apply();
    }

    /**
     * 获取跳过的版本代码
     *
     * @param context 上下文
     * @return 跳过的版本代码
     */
    public static long getSkipVersionCode(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getLong(KEY_SKIP_VERSION_CODE, 0);
    }

    /**
     * 设置安装包Uri
     *
     * @param context 上下文
     * @param uriStr  安装包Uri字符串
     */
    public static void setApkUri(@NonNull Context context, String uriStr) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_APK_URI, uriStr).apply();
    }

    /**
     * 获取安装包Uri
     *
     * @param context 上下文
     * @return 安装包Uri字符串
     */
    public static String getApkUri(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_APK_URI, "");
    }
}
