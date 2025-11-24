package com.project.manager.ui.setting;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeModeHelper {
    // 可选的App主题模式
    public static final int LIGHT_MODE = 0;
    public static final int DARK_MODE = 1;
    public static final int FOLLOW_SYSTEM = 2;

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
            case FOLLOW_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
