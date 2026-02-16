package com.manager.assistant.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.Shapeable;
import com.manager.assistant.ui.others.listeners.SpringAnimationOnTouchListener;

public class AnimationHelper {

    /**
     * 将根布局内的所有MaterialButton和FAB组件添加点击时的圆角变化动画
     *
     * @param root 根布局
     */
    public static void setupAllChildMorphAnimation(@NonNull ViewGroup root) {
        for (int index = 0; index < root.getChildCount(); index++) {
            View child = root.getChildAt(index);
            if (child instanceof MaterialButton || child instanceof FloatingActionButton) {
                attachMorphAnimation(child);
            } else if (child instanceof ViewGroup) {
                setupAllChildMorphAnimation((ViewGroup) child);
            }
        }
    }

    /**
     * 为任何实现了 Shapeable 接口的 View 添加圆角变形动画（按下的圆角为8dp）
     *
     * @param view 目标视图 (如 MaterialButton, FAB 等)
     */
    public static void attachMorphAnimation(View view) {
        attachMorphAnimation(view, 0.4f);
    }

    /**
     * 为任何实现了 Shapeable 接口的 View 添加圆角变形动画（每个角分别计算圆角半径）
     *
     * @param view       目标视图 (如 MaterialButton, FAB 等)
     * @param percentage 按下时的圆角半径与初始圆角半径的比例 (单位: dp)
     */
    public static void attachMorphAnimation(View view, float percentage) {
        if (!(view instanceof Shapeable)) {
            throw new IllegalArgumentException("View must implement Shapeable");
        }

        Shapeable shapeable = (Shapeable) view;
        Vibrator vibrator = (Vibrator) view.getContext()
                .getSystemService(Context.VIBRATOR_SERVICE);

        view.setOnTouchListener(new SpringAnimationOnTouchListener(shapeable, vibrator, percentage));
    }

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
            view.requestLayout();
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
        //目标状态与当前状态相同则不执行动画
        int originVisibility = view.getVisibility();
        if (originVisibility == View.GONE && !isExpanded) {
            return;
        } else if (originVisibility == View.VISIBLE && isExpanded) {
            return;
        }

        //测量视图
        int widthSpec = View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getWidth(), View.MeasureSpec.AT_MOST);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthSpec, heightSpec);

        //执行动画
        view.post(() -> {
            int layout_height = view.getMeasuredHeight();   //获得测量的高度
            if (isExpanded) {
                view.setVisibility(View.VISIBLE);
                AnimationHelper.animateHeight(view, 0, layout_height, null);
            } else {
                AnimationHelper.animateHeight(view, layout_height, 0, () -> view.setVisibility(View.GONE));
            }
        });
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
                    360f,
                    180f
            );
        }

        animator.setDuration(250);
        animator.setInterpolator(new LinearInterpolator()); //匀速
        animator.start();
    }
}
