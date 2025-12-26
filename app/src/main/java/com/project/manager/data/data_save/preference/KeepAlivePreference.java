package com.project.manager.data.data_save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/**
 * 后台保活相关设置的Preference
 */
public class KeepAlivePreference {
    private static final String PREF_NAME = "KeepAlivePreference";
    private static final String KEY_HIDE_RECENTS = "key_hide_recents";  //最近任务列表隐藏

    /**
     * 获取在最近任务列表隐藏状态
     *
     * @param context 上下文
     * @return 是否隐藏
     */
    public static boolean getHideRecents(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_HIDE_RECENTS, false);
    }

    /**
     * 保存在最近任务列表的隐藏状态
     *
     * @param isHide  是否隐藏
     * @param context 上下文
     */
    public static void setHideRecents(boolean isHide, @NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_HIDE_RECENTS, isHide).apply();
    }
}
