package com.manager.assistant.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.RectF;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.ShapeAppearanceModel;
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

        view.setOnTouchListener(new View.OnTouchListener() {
            private float tl, tr, bl, br;                               //初始四个角的圆角值
            private float tlPressed, trPressed, blPressed, brPressed;   //按下时四个角的圆角值
            private boolean initialized = false;                        //标记是否按下过
            private static final long MORPH_DURATION = 120;             //动画持续时间
            private static final float PRESSED_SCALE = 0.94f;           //按下时缩放程度
            private ValueAnimator cornerAnimator;                       //圆角动画执行器
            private SpringAnimation scaleXAnim;                         //X轴缩放动画
            private SpringAnimation scaleYAnim;                         //Y轴缩放动画

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (!initialized && v.getWidth() > 0) {

                    RectF rect = new RectF(0, 0, v.getWidth(), v.getHeight());
                    ShapeAppearanceModel model = shapeable.getShapeAppearanceModel();

                    tl = model.getTopLeftCornerSize().getCornerSize(rect);
                    tr = model.getTopRightCornerSize().getCornerSize(rect);
                    bl = model.getBottomLeftCornerSize().getCornerSize(rect);
                    br = model.getBottomRightCornerSize().getCornerSize(rect);

                    tlPressed = tl * percentage;
                    trPressed = tr * percentage;
                    blPressed = bl * percentage;
                    brPressed = br * percentage;

                    initialized = true;
                }

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        performHaptic(vibrator);
                        ensureSpring(v);
                        scaleXAnim.animateToFinalPosition(PRESSED_SCALE);
                        scaleYAnim.animateToFinalPosition(PRESSED_SCALE);
                        animateElevation(v, true);
                        animateCorners(shapeable,
                                tl, tr, bl, br,
                                tlPressed, trPressed, blPressed, brPressed);
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        ensureSpring(v);
                        scaleXAnim.animateToFinalPosition(1f);
                        scaleYAnim.animateToFinalPosition(1f);
                        animateElevation(v, false);
                        animateCorners(shapeable,
                                tlPressed, trPressed, blPressed, brPressed,
                                tl, tr, bl, br);
                        break;
                }

                return false;
            }

            /**
             * 确保缩放动画执行器已实例化
             *
             * @param v 需要缩放的视图
             */
            private void ensureSpring(View v) {
                if (scaleXAnim == null) {
                    scaleXAnim = new SpringAnimation(v, SpringAnimation.SCALE_X);
                    scaleYAnim = new SpringAnimation(v, SpringAnimation.SCALE_Y);

                    SpringForce forceX = new SpringForce(1f);
                    SpringForce forceY = new SpringForce(1f);

                    forceX.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
                    forceY.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);

                    forceX.setStiffness(SpringForce.STIFFNESS_LOW);
                    forceY.setStiffness(SpringForce.STIFFNESS_LOW);

                    scaleXAnim.setSpring(forceX);
                    scaleYAnim.setSpring(forceY);
                }
            }

            /**
             * 执行阴影动画
             * @param v 需要执行动画的视图
             * @param pressed 是否按下
             */
            private void animateElevation(@NonNull View v, boolean pressed) {
                float target = pressed ? v.getElevation() * 0.6f : v.getElevation();
                v.animate()
                        .translationZ(target)
                        .setDuration(100)
                        .start();
            }

            /**
             * 执行圆角动画
             * @param target 需要执行动画的Shapeable实例
             * @param fromTL 左上角起始
             * @param fromTR 右上角起始
             * @param fromBL 左下角起始
             * @param fromBR 右下角起始
             * @param toTL   左上角结束
             * @param toTR   右上角结束
             * @param toBL   左下角结束
             * @param toBR   右下角结束
             */
            private void animateCorners(
                    Shapeable target,
                    float fromTL, float fromTR, float fromBL, float fromBR,
                    float toTL, float toTR, float toBL, float toBR) {

                //若动画正在运行，则在当前位置反向运行
                if (cornerAnimator != null && cornerAnimator.isRunning()) {
                    cornerAnimator.reverse();
                    return;
                }

                cornerAnimator = ValueAnimator.ofFloat(0f, 1f);
                cornerAnimator.setDuration(MORPH_DURATION);
                cornerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

                cornerAnimator.addUpdateListener(animation -> {

                    float f = animation.getAnimatedFraction();

                    float currentTL = fromTL + (toTL - fromTL) * f;
                    float currentTR = fromTR + (toTR - fromTR) * f;
                    float currentBL = fromBL + (toBL - fromBL) * f;
                    float currentBR = fromBR + (toBR - fromBR) * f;

                    target.setShapeAppearanceModel(
                            target.getShapeAppearanceModel()
                                    .toBuilder()
                                    .setTopLeftCornerSize(currentTL)
                                    .setTopRightCornerSize(currentTR)
                                    .setBottomLeftCornerSize(currentBL)
                                    .setBottomRightCornerSize(currentBR)
                                    .build()
                    );
                });

                cornerAnimator.start();
            }

            private void performHaptic(Vibrator vibrator) {
                if (vibrator == null) return;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    );
                } else {
                    vibrator.vibrate(
                            VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                    );
                }
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
        //测量视图
        int widthSpec = View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getWidth(), View.MeasureSpec.AT_MOST);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthSpec, heightSpec);

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
