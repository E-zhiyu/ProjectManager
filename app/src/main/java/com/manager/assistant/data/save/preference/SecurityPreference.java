package com.manager.assistant.data.save.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.manager.assistant.auxiliary.enums.settings.AuthOpportunity;

public class SecurityPreference {
    private static final String PREF_NAME = "SecurityPreference";
    private static final String KEY_AUTH_SWITCH = "auth_switch";            //身份验证开关
    private static final String KEY_AUTH_OPPORTUNITY = "auth_opportunity";  //身份验证时机

    /**
     * 设置身份验证开关状态
     *
     * @param context  上下文
     * @param isOpened 是否打开
     */
    public static void setAuthSwitchStat(@NonNull Context context, boolean isOpened) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_AUTH_SWITCH, isOpened).apply();
    }

    /**
     * 获取身份验证开关状态
     *
     * @param context 上下文
     * @return 开关是否打开（默认关闭）
     */
    public static boolean getAuthSwitchStat(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_AUTH_SWITCH, false);
    }

    /**
     * 设置身份验证时机
     *
     * @param context 上下文
     * @param code    时机代码，即{@link AuthOpportunity}的枚举序数
     */
    public static void setAuthOpportunity(@NonNull Context context, int code) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(KEY_AUTH_OPPORTUNITY, code).apply();
    }

    /**
     * 获取身份验证时机
     *
     * @param context 上下文
     * @return 时机代码，即{@link AuthOpportunity}的枚举序数
     */
    public static int getAuthOpportunity(@NonNull Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_AUTH_OPPORTUNITY, 0);
    }
}
