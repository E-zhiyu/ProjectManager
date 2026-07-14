package com.manager.assistant.ui.others.listeners;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;

public class SpringAnimationOnTouchListener implements View.OnTouchListener {
    private static final long MORPH_DURATION = 120;             //动画持续时间
    private static final float PRESSED_SCALE = 0.94f;           //按下时缩放程度
    private final float cornerPercentage;                       //圆角半径
    private ValueAnimator cornerAnimator;                       //圆角动画执行器
    private SpringAnimation scaleXAnim;                         //X轴缩放动画
    private SpringAnimation scaleYAnim;                         //Y轴缩放动画
    private final Shapeable shapeable;                          //需要执行动画的Shapeable
    private final Vibrator vibrator;                            //视图的振动器

    public SpringAnimationOnTouchListener(Shapeable shapeable, Vibrator vibrator, float cornerPercentage) {
        this.shapeable = shapeable;
        this.vibrator = vibrator;
        this.cornerPercentage = cornerPercentage;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, @NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performHaptic(vibrator);
                ensureSpring(v);
                scaleXAnim.animateToFinalPosition(PRESSED_SCALE);
                scaleYAnim.animateToFinalPosition(PRESSED_SCALE);
                animateElevation(v, true);
                animateCorners(shapeable, v, true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                ensureSpring(v);
                scaleXAnim.animateToFinalPosition(1f);
                scaleYAnim.animateToFinalPosition(1f);
                animateElevation(v, false);
                animateCorners(shapeable, v, false);
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
     *
     * @param v       需要执行动画的视图
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
     *
     * @param target 需要执行动画的Shapeable实例
     */
    private void animateCorners(Shapeable target, View v, boolean isPressed) {
        //若动画正在运行，则在当前位置反向运行
        if (cornerAnimator != null && cornerAnimator.isRunning()) {
            cornerAnimator.reverse();
            return;
        }

        ShapeAppearanceModel model = shapeable.getShapeAppearanceModel();

        //获取四个圆角值
        RectF rect = new RectF(0, 0, v.getWidth(), v.getHeight());
        float tl = model.getTopLeftCornerSize().getCornerSize(rect);
        float tr = model.getTopRightCornerSize().getCornerSize(rect);
        float bl = model.getBottomLeftCornerSize().getCornerSize(rect);
        float br = model.getBottomRightCornerSize().getCornerSize(rect);

        //计算起止圆角值
        float fromTL, fromTR, fromBL, fromBR;
        float toTL, toTR, toBL, toBR;
        if (isPressed) {
            fromTL = tl;
            fromTR = tr;
            fromBL = bl;
            fromBR = br;

            toTL = fromTL * cornerPercentage;
            toTR = fromTR * cornerPercentage;
            toBL = fromBL * cornerPercentage;
            toBR = fromBR * cornerPercentage;
        } else {
            fromTL = tl;
            fromTR = tr;
            fromBL = bl;
            fromBR = br;

            toTL = fromTL / cornerPercentage;
            toTR = fromTR / cornerPercentage;
            toBL = fromBL / cornerPercentage;
            toBR = fromBR / cornerPercentage;
        }

        //生成动画执行器
        cornerAnimator = ValueAnimator.ofFloat(0f, 1f);
        cornerAnimator.setDuration(MORPH_DURATION);
        cornerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        //绘制每帧动画的圆角
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

        //启动动画
        cornerAnimator.start();
    }

    /**
     * 执行触觉反馈
     *
     * @param vibrator 视图的振动器
     */
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
}
