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
    private float tl, tr, bl, br;                               //初始四个角的圆角值
    private float tlPressed, trPressed, blPressed, brPressed;   //按下时四个角的圆角值
    private boolean initialized = false;                        //标记是否按下过
    private static final long MORPH_DURATION = 120;             //动画持续时间
    private static final float PRESSED_SCALE = 0.94f;           //按下时缩放程度
    private float cornerPercentage = 0.4f;                      //圆角半径
    private ValueAnimator cornerAnimator;                       //圆角动画执行器
    private SpringAnimation scaleXAnim;                         //X轴缩放动画
    private SpringAnimation scaleYAnim;                         //Y轴缩放动画
    private final Shapeable shapeable;                          //需要执行动画的Shapeable
    private final Vibrator vibrator;                            //视图的振动器

    public SpringAnimationOnTouchListener(Shapeable shapeable, Vibrator vibrator) {
        this.shapeable = shapeable;
        this.vibrator = vibrator;
    }

    public SpringAnimationOnTouchListener(Shapeable shapeable, Vibrator vibrator, float cornerPercentage) {
        this.shapeable = shapeable;
        this.vibrator = vibrator;
        this.cornerPercentage = cornerPercentage;
    }

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

            tlPressed = tl * cornerPercentage;
            trPressed = tr * cornerPercentage;
            blPressed = bl * cornerPercentage;
            brPressed = br * cornerPercentage;

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
