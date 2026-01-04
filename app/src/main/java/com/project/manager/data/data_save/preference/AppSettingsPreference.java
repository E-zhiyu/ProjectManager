package com.project.manager.data.data_save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.project.manager.helpers.ThemeModeHelper;

public class AppSettingsPreference {
    private static final String PREF_NAME = "ThemePreference";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_DYNAMIC_COLOR = "dynamic_color";
    private static final String KEY_FIRST_SCREEN = "first_screen";
    private static final String KEY_ACCOUNT_DATA_CHANGED = "account_data_changed";  //是否在非BookKeepingFragment子界面中修改流水数据

    public static void setThemeMode(@NonNull Context context, int themeMode) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_THEME_MODE, themeMode).apply();
    }

    public static int getThemeMode(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_THEME_MODE, ThemeModeHelper.FOLLOW_SYSTEM);
    }

    public static void setDynamicColorStat(@NonNull Context context, boolean isOpened) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_DYNAMIC_COLOR, isOpened).apply();
    }

    public static boolean getDynamicColorStat(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_DYNAMIC_COLOR, true);
    }

    public static void setFirstScreen(@NonNull Context context, int screen_code) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_FIRST_SCREEN, screen_code).apply();
    }

    public static int getFirstScreen(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_FIRST_SCREEN, 0);
    }

    public static void setAccountDataChanged(@NonNull Context context, boolean isChanged) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_ACCOUNT_DATA_CHANGED, isChanged).apply();
    }

    public static boolean getAccountDataChanged(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_ACCOUNT_DATA_CHANGED, false);
    }
}
