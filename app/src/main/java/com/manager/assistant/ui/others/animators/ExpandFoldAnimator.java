package com.manager.assistant.ui.others.animators;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;

import androidx.annotation.NonNull;

//TODO:用该类替换所有展开收缩方法
public class ExpandFoldAnimator {
    private static final int TAG_ANIMATOR = -1001;  //标记动画执行器的标识符
    private final View target;                      //待展开和收缩的目标视图
    private boolean isExpanded;                     //视图的初始状态是否为展开

    public ExpandFoldAnimator(View target, boolean isExpanded) {
        this.target = target;
        this.isExpanded = isExpanded;
    }

    public ExpandFoldAnimator(View target) {
        this(target, true);
    }

    public void toggle() {
        if (isExpanded) {
            collapse();
        } else{
            expand();
        }
    }

    // 展开
    public void expand() {
        // 先取消旧动画
        cancelRunningAnimator(target);

        target.measure(
                View.MeasureSpec.makeMeasureSpec(((View) target.getParent()).getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        final int targetHeight = target.getMeasuredHeight();

        target.getLayoutParams().height = target.getHeight();
        target.setVisibility(View.VISIBLE);

        ValueAnimator animator = ValueAnimator.ofInt(target.getHeight(), targetHeight);
        animator.setDuration(300);

        animator.addUpdateListener(animation -> {
            target.getLayoutParams().height = (int) animation.getAnimatedValue();
            target.requestLayout();
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                target.setTag(TAG_ANIMATOR, null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                target.setTag(TAG_ANIMATOR, null);
            }
        });

        target.setTag(TAG_ANIMATOR, animator);
        animator.start();

        isExpanded = true;
    }

    // 收缩
    public void collapse() {
        cancelRunningAnimator(target);

        ValueAnimator animator = ValueAnimator.ofInt(target.getHeight(), 0);
        animator.setDuration(300);

        animator.addUpdateListener(animation -> {
            target.getLayoutParams().height = (int) animation.getAnimatedValue();
            target.requestLayout();
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                target.setVisibility(View.GONE);
                target.setTag(TAG_ANIMATOR, null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                target.setTag(TAG_ANIMATOR, null);
            }
        });

        target.setTag(TAG_ANIMATOR, animator);
        animator.start();

        isExpanded = false;
    }

    // 取消正在执行的动画
    private static void cancelRunningAnimator(@NonNull View view) {
        Object tag = view.getTag(TAG_ANIMATOR);
        if (tag instanceof ValueAnimator) {
            ((ValueAnimator) tag).cancel();
        }
    }
}