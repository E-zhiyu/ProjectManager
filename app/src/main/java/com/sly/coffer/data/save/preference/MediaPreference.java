package com.sly.coffer.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class MediaPreference {
    private static final String PREF_NAME = "MediaPreference";
    private static final String KEY_HDR_DISPLAY = "hdr_display";

    /**
     * 设置 HDR 显示效果的开关状态
     *
     * @param context 上下文
     * @param stat    开关状态
     */
    public static void setHdrDisplay(@NonNull Context context, boolean stat) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_HDR_DISPLAY, stat).apply();
    }

    /**
     * 获取 HDR 显示效果的开关状态
     *
     * @param context 上下文
     * @return 开关状态
     */
    public static boolean getHdrDisplay(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_HDR_DISPLAY, true);
    }
}
