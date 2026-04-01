package com.manager.assistant.helpers.appearence;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * 视图边距有关的帮助器
 */
public class ViewEdgeHelper {
    /**
     * 设置视图与导航栏的距离为20dp
     *
     * @param view    需要确保不被遮挡的视图
     * @param context 上下文
     */
    public static void setMarginToNavigation(View view, Context context) {
        setMarginToNavigation(view, 20, context);
    }

    /**
     * 设置视图与导航栏的距离
     *
     * @param view    需要确保不被遮挡的视图
     * @param dp      视图与底部导航栏的距离，单位为dp
     * @param context 上下文
     */
    public static void setMarginToNavigation(View view, int dp, Context context) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            //获取系统栏的间距（包含导航栏高度）
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            //获取原本的边距
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            //加上导航栏高度并应用到视图
            lp.bottomMargin = systemBars.bottom + dpToPx(context, dp);
            v.setLayoutParams(lp);

            return windowInsets;
        });
    }

    /**
     * 将dp转换为像素
     *
     * @param context 上下文
     * @return 目标dp对应的像素数量
     */
    public static int dpToPx(@NonNull Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
