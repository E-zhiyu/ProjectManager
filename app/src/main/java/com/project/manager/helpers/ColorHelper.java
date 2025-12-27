package com.project.manager.helpers;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.NonNull;

public class ColorHelper {
    /**
     * 获取attr下的颜色
     *
     * @param context 上下文
     * @param resId   颜色资源ID
     * @return 获取到的颜色值
     */
    public static int getAttrColor(@NonNull Context context, int resId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }

    /**
     * 获取用作背景的颜色
     *
     * @param context 上下文
     * @return 背景色颜色值
     */
    public static int getBackgroundColor(Context context) {
        return getAttrColor(context, com.google.android.material.R.attr.colorSurfaceContainerLow);
    }

    /**
     * 获取主题色
     *
     * @param context 上下文
     * @return 主题色颜色值
     */
    public static int getPrimaryColor(Context context) {
        return getAttrColor(context, android.R.attr.colorPrimary);
    }

    /**
     * 获取第二主题色
     *
     * @param context 上下文
     * @return 第二主题色颜色值
     */
    public static int getSecondaryPrimaryColor(Context context) {
        return getAttrColor(context, android.R.attr.colorSecondary);
    }
}
