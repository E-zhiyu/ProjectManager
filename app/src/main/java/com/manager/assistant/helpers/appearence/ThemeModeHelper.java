package com.manager.assistant.helpers.appearence;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeModeHelper {
    //可选的App主题模式
    public static final int LIGHT_MODE = 0;
    public static final int DARK_MODE = 1;
    public static final int FOLLOW_SYSTEM = 2;

    /**
     * 应用当前选定的主题
     *
     * @param themeMode 深色模式代码
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

    /**
     * 切换深色模式并播放过渡动画
     *
     * @param activity  当前显示的活动界面
     * @param nightMode 深色模式代码
     */
    public static void switchNightModeWithAnimation(@NonNull Activity activity, int nightMode) {
        // 1. 获取根布局
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView().getRootView();

        // 2. 创建覆盖 View
        View overlay = new View(activity);
        overlay.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        overlay.setBackgroundColor(Color.BLACK);
        overlay.setAlpha(0f);
        rootView.addView(overlay);

        // 3. 淡入动画
        overlay.animate()
                .alpha(1f)
                .setDuration(200) // 可以调整时长
                .withEndAction(() -> {
                    // 4. 切换夜间模式
                    applyTheme(nightMode);

                    // 5. 淡出动画
                    overlay.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .withEndAction(() -> rootView.removeView(overlay))
                            .start();
                })
                .start();
    }
}
