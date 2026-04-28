package com.manager.assistant.generic_enums.options;

import androidx.appcompat.app.AppCompatDelegate;

public enum ThemeMode {
    LIGHT_MODE("浅色模式", AppCompatDelegate.MODE_NIGHT_NO),
    DARK_MODE("深色模式",AppCompatDelegate.MODE_NIGHT_YES),
    FOLLOW_SYSTEM("跟随系统",AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    private final String title;
    private final int code;

    ThemeMode(String title, int code) {
        this.title = title;
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public int getCode() {
        return code;
    }
}
