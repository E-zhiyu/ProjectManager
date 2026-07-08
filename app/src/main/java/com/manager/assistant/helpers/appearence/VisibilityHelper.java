package com.manager.assistant.helpers.appearence;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.transition.ChangeBounds;
import androidx.transition.Fade;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

public class VisibilityHelper {
    /**
     * 使用淡入淡出动画切换视图可见性
     *
     * @param view      需要切换可见性的视图
     * @param isVisible 是否可见
     */
    public static void toggleVisibilityWithFade(View view, boolean isVisible) {
        toggleVisibilityWithFade(view, isVisible, 250);
    }

    /**
     * 使用淡入淡出动画切换视图可见性
     *
     * @param view      需要切换可见性的视图
     * @param isVisible 是否可见
     * @param duration  动画持续时间
     */
    public static void toggleVisibilityWithFade(View view, boolean isVisible, int duration) {
        if (isVisible && view.getVisibility() == View.GONE) {
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);
            view.animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        } else if (!isVisible && view.getVisibility() == View.VISIBLE) {
            view.animate()
                    .alpha(0f)
                    .setDuration(duration)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .withEndAction(() -> view.setVisibility(View.GONE))
                    .start();
        }
    }

    /**
     * 通用的视图折叠/展开（显示/隐藏）动画方法
     *
     * @param sceneRoot  动画作用的父容器（如 AppBarLayout, CoordinatorLayout, LinearLayout 等）
     * @param isVisible  true 为展开(VISIBLE)，false 为折叠(GONE)
     * @param endAction  动画结束后的回调闭包（可用于清空数据、释放资源等），可传 null
     * @param targetView 要显示或隐藏的根目标视图
     */
    public static void toggleViewExpansion(
            @NonNull ViewGroup sceneRoot,
            boolean isVisible,
            @Nullable Runnable endAction,
            @NonNull View... targetView) {
        toggleViewExpansion(sceneRoot, isVisible, 250, endAction, targetView);
    }

    /**
     * 通用的视图折叠/展开（显示/隐藏）动画方法
     *
     * @param sceneRoot   动画作用的父容器（如 AppBarLayout, CoordinatorLayout, LinearLayout 等）
     * @param isVisible   true 为展开(VISIBLE)，false 为折叠(GONE)
     * @param duration    动画时长（毫秒）
     * @param endAction   动画结束后的回调闭包（可用于清空数据、释放资源等），可传 null
     * @param targetViews 要显示或隐藏的根目标视图
     */
    public static void toggleViewExpansion(
            @NonNull ViewGroup sceneRoot,
            boolean isVisible,
            long duration,
            @Nullable Runnable endAction,
            @NonNull View... targetViews) {
        if (targetViews.length == 0) return;

        //组装动画集：ChangeBounds(负责父容器平滑折叠) + Slide/Fade(负责子视图平滑过渡)
        TransitionSet transitionSet = new TransitionSet()
                .setOrdering(TransitionSet.ORDERING_TOGETHER)
                .setInterpolator(new FastOutSlowInInterpolator())
                .addTransition(new ChangeBounds())
                .setDuration(duration);

        int i = 0;
        int targetVisibility = isVisible ? View.VISIBLE : View.GONE;
        for (View targetView : targetViews) {
            if (targetView.getVisibility() == targetVisibility) continue;

            if (isVisible) {
                transitionSet.addTransition(new Fade(Fade.IN));
            } else {
                transitionSet.addTransition(new Fade(Fade.OUT));
            }

            //设置动画结束的回调监听（仅当最后一个视图动画执行完毕后）
            if (i == targetViews.length - 1) {
                transitionSet.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionEnd(@NonNull Transition transition) {
                        // 核心：动画完全结束后，安全执行用户传递的代码
                        if (endAction != null) {
                            endAction.run();
                        }
                        transition.removeListener(this);
                    }

                    @Override
                    public void onTransitionStart(@NonNull Transition transition) {
                    }

                    @Override
                    public void onTransitionCancel(@NonNull Transition transition) {
                    }

                    @Override
                    public void onTransitionPause(@NonNull Transition transition) {
                    }

                    @Override
                    public void onTransitionResume(@NonNull Transition transition) {
                    }
                });
            }

            // 4. 开始执行动画
            TransitionManager.beginDelayedTransition(sceneRoot, transitionSet);
            targetView.setVisibility(targetVisibility);

            i++;
        }
    }
}
