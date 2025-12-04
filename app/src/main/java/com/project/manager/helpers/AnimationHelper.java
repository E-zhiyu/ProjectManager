package com.project.manager.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;

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
        animator.addUpdateListener(animation -> {
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

    /**
     * 切换视图展开或折叠状态（平滑动画）
     *
     * @param isExpanded 是否切换为展开状态
     * @param view       需要执行动画的视图
     */
    public static void switchViewFoldOrExpanded(boolean isExpanded, @NonNull View view) {
        //临时改为可见
        int originVisibility = view.getVisibility();
        view.setVisibility(View.VISIBLE);

        // 测量视图
        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthSpec, heightSpec);
        int layout_height = view.getMeasuredHeight();   //获得测量的高度

        view.setVisibility(originVisibility);   //恢复原来的可见性

        if (isExpanded) {
            view.setVisibility(View.VISIBLE);
            AnimationHelper.animateHeight(view, 0, layout_height, null);
        } else {
            AnimationHelper.animateHeight(view, layout_height, 0, () -> view.setVisibility(View.GONE));
        }
    }

    /**
     * 旋转视图的图标
     *
     * @param expand_fold_view 需要旋转图标的视图
     * @param isExpanded       原先是否为展开状态
     */
    public static void rotateIcon(View expand_fold_view, boolean isExpanded) {
        //使用 ObjectAnimator 动画旋转
        ObjectAnimator animator;
        if (!isExpanded) {
            //不是展开状态，则将旋转了180°的图标旋转至360°
            animator = ObjectAnimator.ofFloat(
                    expand_fold_view,
                    "rotation",
                    180f,
                    360f
            );
        } else {
            animator = ObjectAnimator.ofFloat(
                    expand_fold_view,
                    "rotation",
                    0f,
                    180f
            );
        }

        animator.setDuration(250);
        animator.setInterpolator(new LinearInterpolator()); //匀速
        animator.start();
    }
}
