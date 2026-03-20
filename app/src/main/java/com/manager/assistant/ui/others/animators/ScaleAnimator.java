package com.manager.assistant.ui.others.animators;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;

import androidx.annotation.NonNull;

public class ScaleAnimator {

    private static final int TAG_ANIMATOR = -2001;
    private static final int DURATION_DEFAULT = 400;

    // 显示（缩放 + 渐显）
    public static void show(View view) {
        cancel(view);

        view.setVisibility(View.VISIBLE);

        view.setPivotX(view.getWidth() / 2f);
        view.setPivotY(view.getHeight() / 2f);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(DURATION_DEFAULT);

        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();

            float scale = 0.8f + 0.2f * alpha;

            view.setScaleX(scale);
            view.setScaleY(scale);
            view.setAlpha(alpha);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setTag(TAG_ANIMATOR, null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                view.setTag(TAG_ANIMATOR, null);
            }
        });

        view.setTag(TAG_ANIMATOR, animator);
        animator.start();
    }

    // 隐藏（缩放 + 渐隐）
    public static void hide(View view) {
        cancel(view);

        view.setPivotX(view.getWidth() / 2f);
        view.setPivotY(view.getHeight() / 2f);

        ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
        animator.setDuration(DURATION_DEFAULT);

        animator.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();

            float scale = 0.8f + 0.2f * alpha;

            view.setScaleX(scale);
            view.setScaleY(scale);
            view.setAlpha(alpha);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                view.setTag(TAG_ANIMATOR, null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                view.setTag(TAG_ANIMATOR, null);
            }
        });

        view.setTag(TAG_ANIMATOR, animator);
        animator.start();
    }

    private static void cancel(@NonNull View view) {
        Object tag = view.getTag(TAG_ANIMATOR);
        if (tag instanceof ValueAnimator) {
            ((ValueAnimator) tag).cancel();
        }
    }
}
