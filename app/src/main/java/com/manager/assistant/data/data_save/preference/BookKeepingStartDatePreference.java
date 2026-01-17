package com.manager.assistant.data.data_save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/**
 * 保存记账开始日期的Preference
 */
public class BookKeepingStartDatePreference {
    private static final String PREF_NAME = "BookKeepingStartDatePreference";
    private static final String KEY_START_DATE = "start_date";

    public static void saveStartDate(String start_date, @NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_START_DATE, start_date).apply();
    }

    public static String getStartDate(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_START_DATE, "");
    }
}
