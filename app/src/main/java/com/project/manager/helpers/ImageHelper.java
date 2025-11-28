package com.project.manager.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.annotation.NonNull;

public class ImageHelper {
    /**
     * 将Drawable转换为Bitmap
     *
     * @param drawable     原Drawable图标
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return 转换后的图标
     */
    public static Bitmap drawableToBitmap(Drawable drawable, int targetWidth, int targetHeight) {
        if (drawable instanceof BitmapDrawable) {
            //如果是 BitmapDrawable，直接获取 Bitmap 并缩放
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        }

        //其他类型（VectorDrawable、AdaptiveIconDrawable 等）需要绘制到 Bitmap
        Bitmap bitmap = Bitmap.createBitmap(
                targetWidth,
                targetHeight,
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 将图标缩放至目标大小
     *
     * @param originIcon 原始图标
     * @param size 目标像素大小
     * @param context 上下文
     * @return 缩放后的图标
     */
    public static Drawable resizeIcon(Drawable originIcon, int size, @NonNull Context context) {
        //统一缩放为固定尺寸
        int targetSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                size,
                context.getResources().getDisplayMetrics()
        );

        Drawable scaledIcon = null;
        if (originIcon != null) {
            Bitmap bitmap = drawableToBitmap(originIcon, targetSize, targetSize);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true);
            scaledIcon = new BitmapDrawable(context.getResources(), scaledBitmap);
        }

        return scaledIcon;
    }
}
