package com.manager.assistant.ui.others.animators;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StrikeThroughAnimator {

    private static final int TAG_DRAWABLE = 0x7F0B0001; //Drawable的标识符
    private static final int TAG_ANIMATOR = 0x7F0B0002; //动画执行器的标识符

    /**
     * 应用删除线动画
     *
     * @param textView 需要应用动画的文本视图
     * @param strike   最终状态是否为有删除线的状态
     */
    public static void applyStrikeAnimation(@NonNull TextView textView, boolean strike) {
        View parent = (View) textView.getParent();
        if (parent == null) return;

        // 获取或创建 drawable
        StrikeDrawable drawable = (StrikeDrawable) textView.getTag(TAG_DRAWABLE);
        if (drawable == null) {
            drawable = new StrikeDrawable(textView);
            textView.setTag(TAG_DRAWABLE, drawable);
        }

        if (drawable.getCallback() == null) {
            parent.getOverlay().add(drawable);
        }

        // 取消旧动画
        ValueAnimator oldAnimator = (ValueAnimator) textView.getTag(TAG_ANIMATOR);
        if (oldAnimator != null) {
            oldAnimator.cancel();
        }

        // 从“当前进度”开始动画（防止跳变）
        float current = drawable.getProgress();
        float target = strike ? 1f : 0f;

        if (current == target) return;

        ValueAnimator animator = ValueAnimator.ofFloat(current, target);
        animator.setDuration(250);

        StrikeDrawable finalDrawable = drawable;
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            finalDrawable.setProgress(value);
        });

        // 动画结束后清理
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                textView.setTag(TAG_ANIMATOR, null);
            }
        });

        //开始动画
        textView.setTag(TAG_ANIMATOR, animator);
        animator.start();
    }

    // ================= Drawable =================

    private static class StrikeDrawable extends Drawable {

        private final TextView textView;    //覆盖在文本视图表面的删除线Drawable
        private final Paint paint;
        private float progress = 0f;        //过程占比（0~1）

        public StrikeDrawable(@NonNull TextView textView) {
            this.textView = textView;

            paint = new Paint();
            paint.setColor(textView.getCurrentTextColor());
            paint.setStrokeWidth(4f);
            paint.setAntiAlias(true);
        }

        public void setProgress(float progress) {
            this.progress = progress;
            invalidateSelf();
        }

        public float getProgress() {
            return progress;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (progress <= 0f) return;

            float left = textView.getLeft();
            float top = textView.getTop();
            float width = textView.getWidth();
            float height = textView.getHeight();

            float y = top + height / 2f;

            canvas.drawLine(
                    left,
                    y,
                    left + width * progress,
                    y,
                    paint
            );
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    /**
     * 设置文本颜色
     *
     * @param tv       需要设置颜色的文本视图
     * @param excluded true:文本被排除，为灰色;false:文本未排除，为黑色
     */
    public static void setExcluded(TextView tv, boolean excluded) {
        applyStrikeAnimation(tv, excluded);

        tv.animate()
                .alpha(excluded ? 0.5f : 1f)
                .setDuration(200)
                .start();

        tv.setTextColor(excluded ? Color.GRAY : Color.BLACK);
    }
}
