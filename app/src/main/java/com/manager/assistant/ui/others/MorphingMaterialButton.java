package com.manager.assistant.ui.others; // 请根据你的项目修改包名

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.shape.ShapeAppearanceModel;

public class MorphingMaterialButton extends MaterialButton {

    private float initialCornerSize = 0f;
    private float pressedCornerSize = 0f;
    private ValueAnimator animator;

    public MorphingMaterialButton(@NonNull Context context) {
        this(context, null);
    }

    public MorphingMaterialButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, com.google.android.material.R.attr.materialButtonStyle);
    }

    public MorphingMaterialButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 1. 立即获取当前的形状模型，而不是等待 post
        // 即使 RectF 此时是空的，对于以 dp 为单位的圆角，它也能返回正确的值
        updateInitialCornerSize();

        // 2. 增加布局监听，确保在尺寸变化时同步更新
        addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (initialCornerSize == 0) {
                updateInitialCornerSize();
            }
        });
    }

    private void updateInitialCornerSize() {
        ShapeAppearanceModel model = getShapeAppearanceModel();
        // 使用一个临时的、足够大的 RectF 来解析圆角大小
        // 这样可以避免由于按钮尚未测量（width=0）导致的获取失败
        RectF tempRect = new RectF(0, 0, 1000, 1000);
        initialCornerSize = model.getTopLeftCornerSize().getCornerSize(tempRect);
        pressedCornerSize = initialCornerSize * 0.4f;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        // 增加保护：如果尚未获取到初始圆角，不执行动画逻辑
        if (initialCornerSize <= 0) {
            updateInitialCornerSize();
            return;
        }

        boolean pressed = isPressed();
        float targetSize = pressed ? pressedCornerSize : initialCornerSize;

        // 检查是否需要启动动画
        startShapeAnimation(targetSize);
    }

    private void startShapeAnimation(float toSize) {
        if (animator != null) {
            animator.cancel();
        }

        // 获取当前实时的圆角大小作为起点
        float currentSize = getShapeAppearanceModel().getTopLeftCornerSize().getCornerSize(getRectF());

        animator = ValueAnimator.ofFloat(currentSize, toSize);
        animator.setDuration(150); // M3 标准动画时长
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            // 重新构建形状模型并应用
            setShapeAppearanceModel(
                    getShapeAppearanceModel().toBuilder()
                            .setAllCornerSizes(animatedValue)
                            .build()
            );
        });

        animator.start();
    }

    private RectF getRectF() {
        return new RectF(0, 0, getWidth(), getHeight());
    }
}