package com.manager.assistant.ui.others.animators;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

public class RotateAnimator {
    private final ObjectAnimator animator;  //旋转视图的动画器
    private boolean isAtEnd;                //是否在最终状态

    /**
     * 旋转动画执行器构造方法（指定初始状态）
     *
     * @param target    需要旋转的视图
     * @param start     起始角度
     * @param end       结束角度
     * @param isReverse 是否反过来执行
     */
    public RotateAnimator(View target, float start, float end, boolean isReverse) {
        if (!isReverse) {
            animator = ObjectAnimator.ofFloat(
                    target,
                    "rotation",
                    start,
                    end
            );
        } else {
            animator = ObjectAnimator.ofFloat(
                    target,
                    "rotation",
                    end,
                    start
            );
        }
        animator.setDuration(250);
        animator.setInterpolator(new AccelerateInterpolator()); //调速器

        this.isAtEnd = isReverse;
    }

    /**
     * 旋转动画执行器构造方法
     *
     * @param target 需要旋转的视图
     * @param start  起始角度
     * @param end    结束角度
     */
    public RotateAnimator(View target, float start, float end) {
        this(target, start, end, false);
    }

    /**
     * 切换旋转状态
     */
    public void toggle() {
        if (!isAtEnd) {
            animator.start();
        } else {
            animator.reverse();
        }

        isAtEnd = !isAtEnd;
    }
}
