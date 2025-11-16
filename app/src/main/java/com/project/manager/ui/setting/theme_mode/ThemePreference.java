package com.project.manager.ui.setting.theme_mode;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class ThemePreference {
    private static final String PREF_NAME = "ThemePreference";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static void saveThemeMode(@NonNull Context context, int themeMode) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_THEME_MODE, themeMode).apply();
    }

    public static int getThemeMode(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_THEME_MODE, ThemeModeHelper.SYSTEM_MODE);
    }
}
