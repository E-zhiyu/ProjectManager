package com.project.manager.data_save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class AutoBookKeepingPreference {
    private static final String PREF_NAME = "AutoBookKeepingPreference";
    private static final String KEY_NOTIFICATION_ANALYSIS_OPENED = "notification_analysis_opened";

    public static void setNotificationAnalysisOpened(boolean isOpened, @NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_NOTIFICATION_ANALYSIS_OPENED, isOpened).apply();
    }

    public static boolean getNotificationAnalysisOpened(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_NOTIFICATION_ANALYSIS_OPENED, false);
    }
}
