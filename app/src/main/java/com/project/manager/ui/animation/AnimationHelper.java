package com.project.manager.ui.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

public class AnimationHelper {
    /**
     * 执行平滑滑动动画
     *
     * @param view  需要执行动画的视图
     * @param start 开始时视图的高度
     * @param end   结束时视图的高度
     * @param onEnd 动画结束后需要执行的代码块（可为null）
     */
    public static void animateHeight(final View view, int start, int end, Runnable onEnd) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener((animation) -> {
            int height = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = height;
            view.setLayoutParams(layoutParams);
        });

        //设置动画结束后执行的代码
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onEnd != null) onEnd.run();
            }
        });

        animator.setDuration(200);
        animator.start();
    }
}
