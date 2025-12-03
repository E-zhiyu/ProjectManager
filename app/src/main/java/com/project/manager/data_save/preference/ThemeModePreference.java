package com.project.manager.data_save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.project.manager.helpers.ThemeModeHelper;

public class ThemeModePreference {
    private static final String PREF_NAME = "ThemePreference";
    private static final String KEY_THEME_MODE = "theme_mode";

    public static void saveThemeMode(@NonNull Context context, int themeMode) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_THEME_MODE, themeMode).apply();
    }

    public static int getThemeMode(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_THEME_MODE, ThemeModeHelper.FOLLOW_SYSTEM);
    }
}
