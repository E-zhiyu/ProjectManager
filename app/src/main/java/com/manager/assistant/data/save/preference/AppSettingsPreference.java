package com.manager.assistant.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.manager.assistant.auxiliary.enums.options.ThemeMode;

public class AppSettingsPreference {
    private static final String PREF_NAME = "ThemePreference";
    private static final String KEY_THEME_MODE = "theme_mode";          //主题模式
    private static final String KEY_DYNAMIC_COLOR = "dynamic_color";    //动态色彩
    private static final String KEY_FIRST_SCREEN = "first_screen";      //开屏界面

    public static void setThemeMode(@NonNull Context context, int themeMode) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_THEME_MODE, themeMode).apply();
    }

    public static int getThemeMode(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_THEME_MODE, ThemeMode.FOLLOW_SYSTEM.ordinal());
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
        return pref.getInt(KEY_FIRST_SCREEN, 1);
    }
}
