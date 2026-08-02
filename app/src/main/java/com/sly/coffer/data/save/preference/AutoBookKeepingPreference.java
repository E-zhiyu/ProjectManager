package com.sly.coffer.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/**
 * 自动记账相关设置的Preference
 */
public class AutoBookKeepingPreference {
    private static final String PREF_NAME = "AutoBookKeepingPreference";
    private static final String KEY_NOTIFICATION_ANALYSIS_OPENED = "notification_analysis_opened";
    private static final String KEY_NOTIFICATION_CANCEL = "notification_cancel";    //自动记账通知点击行为
    private static final String KEY_NOTIFICATION_CLICK = "notification_click";      //自动记账确认通知点击行为

    public static void setSwitchStat(boolean isOpened, @NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_NOTIFICATION_ANALYSIS_OPENED, isOpened).apply();
    }

    public static boolean getSwitchStat(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_NOTIFICATION_ANALYSIS_OPENED, false);
    }

    /**
     * 设置通知划走后是否保存流水记录
     *
     * @param context   上下文
     * @param behaviour 是否保存流水记录（0：保留，1：不保留）
     */
    public static void setNotificationCancelBehaviour(@NonNull Context context, int behaviour) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_NOTIFICATION_CANCEL, behaviour).apply();
    }

    /**
     * 读取通知划走后是否保存流水记录
     *
     * @param context 上下文
     * @return 是否保存流水记录（0：保留，1：不保留）
     */
    public static int getNotificationCancelBehaviour(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_NOTIFICATION_CANCEL, 0);
    }

    /**
     * 设置通知划走后是否保存流水记录
     *
     * @param context   上下文
     * @param behaviour 是否保存流水记录（0：保留，1：不保留）
     */
    public static void setNotificationClickBehaviour(@NonNull Context context, int behaviour) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_NOTIFICATION_CLICK, behaviour).apply();
    }

    /**
     * 读取通知划走后是否保存流水记录
     *
     * @param context 上下文
     * @return 是否保存流水记录（0：保留，1：不保留）
     */
    public static int getNotificationClickBehaviour(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_NOTIFICATION_CLICK, 0);
    }
}
