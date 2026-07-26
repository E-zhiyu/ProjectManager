package com.manager.assistant.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;

import com.manager.assistant.ui.others.popup.TextPopupWindow;

public class TipPreference {
    private static final String PREF_NAME = "TipPreference";

    /**
     * 保存是否提示过的数据
     *
     * @param context  上下文
     * @param key      该数据的关键字
     * @param tipCount 提醒过的次数
     */
    private static void setValue(@NonNull Context context, String key, int tipCount) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(key, tipCount).apply();
    }

    /**
     * 获取是否提醒过的数据
     *
     * @param context 上下文
     * @param key     关键字
     * @return 是否提醒过
     */
    private static int getValue(@NonNull Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(key, 0);
    }

    /**
     * 自动显示提示文本窗口
     *
     * @param anchor      提示浮窗锚点
     * @param gravity     浮窗显示的方位，具体参考{@link Gravity}中的静态常量
     * @param tipMessage  提示文本
     * @param key         关键字，用于记录该窗口弹出的次数，具体参考{@link TipPreference}中的静态常量
     * @param maxTipCount 最大提醒次数
     */
    public static void showTip(@NonNull View anchor, int gravity, String tipMessage, String key, int maxTipCount) {
        Context context = anchor.getContext();
        int currentValue = getValue(context, key);
        if (currentValue < maxTipCount) {
            TextPopupWindow window = new TextPopupWindow(tipMessage, context);
            window.show(anchor, gravity);

            setValue(context, key, currentValue + 1);
        }
    }

    /**
     * 手动显示提示文本窗口，没有显示次数限制
     *
     * @param anchor  提示浮窗锚点
     * @param gravity 浮窗显示的方位，具体参考{@link Gravity}中的静态常量
     * @param message 提示文本
     */
    public static void showTipWithoutKey(@NonNull View anchor, int gravity, String message) {
        Context context = anchor.getContext();
        TextPopupWindow window = new TextPopupWindow(message, context);
        window.show(anchor, gravity);
    }
}
