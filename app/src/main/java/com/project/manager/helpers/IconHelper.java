package com.project.manager.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

public class IconHelper {
    private static final int TARGET_ICON_SIZE = 48;    //图标的目标大小（dp）

    /**
     * 将dp转换为像素
     *
     * @param context 上下文
     * @return 目标dp对应的像素数量
     */
    private static int dpToPx(@NonNull Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(IconHelper.TARGET_ICON_SIZE * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @NonNull
    public static Bitmap getUniformIconBitmapWithPadding(Context context, @NonNull Drawable drawable) {
        int targetSize = dpToPx(context);

        // 计算原始图标的宽高比
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        float aspectRatio = (float) intrinsicWidth / intrinsicHeight;

        // 计算目标尺寸，保持宽高比
        int destWidth, destHeight;
        if (aspectRatio > 1) {
            // 宽图
            destWidth = targetSize;
            destHeight = Math.round(targetSize / aspectRatio);
        } else {
            // 高图
            destHeight = targetSize;
            destWidth = Math.round(targetSize * aspectRatio);
        }

        // 创建目标Bitmap
        Bitmap output = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // 计算绘制位置（居中）
        int left = (targetSize - destWidth) / 2;
        int top = (targetSize - destHeight) / 2;

        // 绘制图标
        drawable.setBounds(left, top, left + destWidth, top + destHeight);
        drawable.draw(canvas);

        return output;
    }
}
