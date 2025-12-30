package com.project.manager.data.data_save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class VersionPreference {
    private static final String PREF_NAME = "VersionPreference";
    private static final String KEY_SKIP_VERSION_CODE = "skip_version_code";    //跳过更新的版本代码
    private static final String KEY_START_VERSION_CHECK = "start_version_check";    //控制软件启动时运行更新检测的整数值
    public static final int VERSION_CHECK_RECYCLE_NUM = 3;                     //控制每启动几次就检测更新的整数值

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

    /**
     * 写入启动时控制检测更新的整数
     *
     * @param context 上下文
     * @param num     旧数值+1
     */
    public static void setStartVersionCheckNum(@NonNull Context context, int num) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_START_VERSION_CHECK, num).apply();
    }

    /**
     * 获取启动时控制检测更新的整数
     *
     * @param context 上下文
     * @return 启动时控制检测更新的整数
     */
    public static int getStartVersionCheckNum(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_START_VERSION_CHECK, 0);
    }
}
