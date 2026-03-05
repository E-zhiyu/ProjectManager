package com.manager.assistant.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/**
 * 自动记账相关设置的Preference
 */
public class AutoBookKeepingPreference {
    private static final String PREF_NAME = "AutoBookKeepingPreference";
    private static final String KEY_NOTIFICATION_ANALYSIS_OPENED = "notification_analysis_opened";
    private static final String KEY_HINT_AUTO_START = "hint_auto_start";    //是否提示打开自启动权限

    public static void setSwitchStat(boolean isOpened, @NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_NOTIFICATION_ANALYSIS_OPENED, isOpened).apply();
    }

    public static boolean getSwitchStat(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_NOTIFICATION_ANALYSIS_OPENED, false);
    }

    public static void setHintAutoStart(boolean isHinted, @NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_HINT_AUTO_START, isHinted).apply();
    }

    public static boolean getHintAutoStart(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_HINT_AUTO_START, false);
    }
}
