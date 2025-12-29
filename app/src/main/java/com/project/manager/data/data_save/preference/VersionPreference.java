package com.project.manager.data.data_save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class VersionPreference {
    private static final String PREF_NAME = "VersionPreference";
    private static final String KEY_SKIP_VERSION_CODE = "skip_version_code";

    /**
     * 设置跳过的版本代码
     *
     * @param context      上下文
     * @param version_code 版本代码
     */
    public static void setSkipVersionCode(@NonNull Context context, int version_code) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_SKIP_VERSION_CODE, version_code).apply();
    }

    /**
     * 获取跳过的版本代码
     *
     * @param context 上下文
     * @return 跳过的版本代码
     */
    public static int getSkipVersionCode(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_SKIP_VERSION_CODE, 0);
    }
}
