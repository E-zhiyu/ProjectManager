package com.project.manager.ui.setting.theme_mode;

import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeModeHelper {
    // 可选的App主题模式
    public static final int LIGHT_MODE = 0;
    public static final int DARK_MODE = 1;
    public static final int SYSTEM_MODE = 2;

    /**
     * 应用当前选定的主题
     */
    public static void applyTheme(int themeMode) {
        switch (themeMode) {
            case LIGHT_MODE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK_MODE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case SYSTEM_MODE:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    /**
     * 获取当前主题模式
     */
    public static int getCurrentTheme(Context context) {
        int nightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;

        switch (nightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                return LIGHT_MODE;
            case Configuration.UI_MODE_NIGHT_YES:
                return DARK_MODE;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
            default:
                return SYSTEM_MODE;
        }
    }
}
