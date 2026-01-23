package com.manager.assistant.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.Shapeable;

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
        attachMorphAnimation(view, 8);
    }

    /**
     * 为任何实现了 Shapeable 接口的 View 添加圆角变形动画
     *
     * @param view            目标视图 (如 MaterialButton, FAB 等)
     * @param pressedCornerDp 按下时的圆角大小 (单位: dp)
     */
    public static void attachMorphAnimation(View view, float pressedCornerDp) {
        if (!(view instanceof Shapeable)) {
            throw new IllegalArgumentException("视图必须实现Shapeable接口");
        }

        Shapeable shapeable = (Shapeable) view;

        //将 dp 转换为 px
        float pressedPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                pressedCornerDp,
                view.getResources().getDisplayMetrics()
        );

        //监听触摸事件
        view.setOnTouchListener(new View.OnTouchListener() {
            private float initialPx = -1f;
            private ValueAnimator animator;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 首次触摸时获取初始圆角大小
                if (initialPx == -1f) {
                    RectF rect = new RectF(0, 0, v.getWidth(), v.getHeight());
                    initialPx = shapeable.getShapeAppearanceModel()
                            .getTopLeftCornerSize()
                            .getCornerSize(rect);
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startAnimation(shapeable, initialPx, pressedPx);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        startAnimation(shapeable, pressedPx, initialPx);
                        break;
                }

                //返回false，确保不会拦截点击事件
                return false;
            }

            private void startAnimation(Shapeable target, float from, float to) {
                //若动画正在运行，则在当前位置反向运行
                if (animator != null && animator.isRunning()) {
                    animator.reverse();
                    return;
                }

                animator = ValueAnimator.ofFloat(from, to);
                animator.setDuration(150);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.addUpdateListener(animation -> {
                    float value = (float) animation.getAnimatedValue();
                    target.setShapeAppearanceModel(
                            target.getShapeAppearanceModel().toBuilder()
                                    .setAllCornerSizes(value)
                                    .build()
                    );
                });
                animator.start();
            }
        });
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
        // 测量视图
        int widthSpec = View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getWidth(), View.MeasureSpec.AT_MOST);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthSpec, heightSpec);

        //临时改为可见
        int originVisibility = view.getVisibility();
        view.setVisibility(View.VISIBLE);
        view.requestLayout(); //强制触发布局流程

        view.post(() -> {
            int layout_height = view.getMeasuredHeight();   //获得测量的高度
            if (isExpanded) {
                view.setVisibility(View.VISIBLE);
                AnimationHelper.animateHeight(view, 0, layout_height, null);
            } else {
                AnimationHelper.animateHeight(view, layout_height, 0, () -> view.setVisibility(View.GONE));
            }
        });

        view.setVisibility(originVisibility);   //恢复原来的可见性
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
