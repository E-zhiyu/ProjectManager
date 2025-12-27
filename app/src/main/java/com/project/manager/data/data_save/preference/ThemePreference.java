package com.project.manager.data.data_save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.project.manager.helpers.ThemeModeHelper;

/**
 * 应用主题的Preference
 */
public class ThemePreference {
    private static final String PREF_NAME = "ThemePreference";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_DYNAMIC_COLOR = "dynamic_color";

    public static void saveThemeMode(@NonNull Context context, int themeMode) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_THEME_MODE, themeMode).apply();
    }

    public static int getThemeMode(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_THEME_MODE, ThemeModeHelper.FOLLOW_SYSTEM);
    }

    public static void saveDynamicColorStat(@NonNull Context context, boolean isOpened) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_DYNAMIC_COLOR, isOpened).apply();
    }

    public static boolean getDynamicColorStat(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_DYNAMIC_COLOR, false);
    }
}
