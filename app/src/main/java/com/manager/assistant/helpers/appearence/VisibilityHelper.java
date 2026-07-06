package com.manager.assistant.helpers.appearence;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.transition.ChangeBounds;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

    // 使用注解定义支持的折叠/展开方向
    @IntDef({Gravity.TOP, Gravity.BOTTOM, Gravity.START, Gravity.END})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationDirection {
    }

    /**
     * 通用的视图折叠/展开（显示/隐藏）动画方法
     *
     * @param sceneRoot  动画作用的父容器（如 AppBarLayout, CoordinatorLayout, LinearLayout 等）
     * @param targetView 要显示或隐藏的根目标视图（如你的 RecyclerView）
     * @param isVisible  true 为展开(VISIBLE)，false 为折叠(GONE)
     * @param direction  滑动方向，例如 Gravity.TOP（向上折叠/从上滑出）
     * @param endAction  动画结束后的回调闭包（可用于清空数据、释放资源等），可传 null
     */
    public static void toggleViewExpansion(
            @NonNull ViewGroup sceneRoot,
            @NonNull View targetView,
            boolean isVisible,
            @AnimationDirection int direction,
            @Nullable Runnable endAction
    ) {
        toggleViewExpansion(sceneRoot, targetView, isVisible, direction, 250, endAction);
    }

    /**
     * 通用的视图折叠/展开（显示/隐藏）动画方法
     *
     * @param sceneRoot  动画作用的父容器（如 AppBarLayout, CoordinatorLayout, LinearLayout 等）
     * @param targetView 要显示或隐藏的根目标视图（如你的 RecyclerView）
     * @param isVisible  true 为展开(VISIBLE)，false 为折叠(GONE)
     * @param direction  滑动方向，例如 Gravity.TOP（向上折叠/从上滑出）
     * @param duration   动画时长（毫秒）
     * @param endAction  动画结束后的回调闭包（可用于清空数据、释放资源等），可传 null
     */
    public static void toggleViewExpansion(
            @NonNull ViewGroup sceneRoot,
            @NonNull View targetView,
            boolean isVisible,
            @AnimationDirection int direction,
            long duration,
            @Nullable Runnable endAction
    ) {
        // 1. 状态防抖：如果目标状态与当前状态相同，则不重复执行动画
        int targetVisibility = isVisible ? View.VISIBLE : View.GONE;
        if (targetView.getVisibility() == targetVisibility) {
            if (endAction != null) endAction.run();
            return;
        }

        // 2. 组装动画集：ChangeBounds(负责父容器平滑折叠) + Slide/Fade(负责子视图平滑过渡)
        TransitionSet transitionSet = new TransitionSet()
                .setOrdering(TransitionSet.ORDERING_TOGETHER)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setDuration(duration);

        if (isVisible) {
            // 【展开动画】从指定方向滑入 + 淡入
            transitionSet.addTransition(new Slide(direction))
                    .addTransition(new Fade(Fade.IN));
        } else {
            // 【折叠动画】由于退出时 Slide 容易被父布局裁剪，采用 ChangeBounds + Fade 组合能实现最完美的“折叠”感
            // 如果你更喜欢硬生生滑出的感觉，可以将 ChangeBounds() 替换为 new Slide(direction)
            transitionSet.addTransition(new ChangeBounds())
                    .addTransition(new Fade(Fade.OUT));
        }

        // 3. 设置动画结束的回调监听
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

        // 4. 开始执行动画
        TransitionManager.beginDelayedTransition(sceneRoot, transitionSet);
        targetView.setVisibility(targetVisibility);
    }
}
