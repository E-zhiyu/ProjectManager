package com.manager.assistant.helpers.resourse;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;

/**
 * 资源帮助器
 */
public class ResHelper {
    /**
     * 获取样式资源
     * @param context 上下文
     * @param attrResId 需要获取的样式ID
     * @return 处理后的资源ID
     */
    public static int getStyleOrThrow(@NonNull Context context, @AttrRes int attrResId) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attrResId, typedValue, true)) {
            return typedValue.data;
        }
        throw new IllegalArgumentException(context.getResources().getResourceName(attrResId));
    }
}
