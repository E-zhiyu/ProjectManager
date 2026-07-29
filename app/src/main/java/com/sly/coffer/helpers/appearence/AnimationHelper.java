package com.sly.coffer.helpers.appearence;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.shape.ShapeAppearanceModel;

public class AnimationHelper {
    /**
     * 播放闪烁动画
     *
     * @param view 需要闪烁的视图
     */
    public static void blink(View view) {
        if (view == null) return;

        // 动态创建一个 GradientDrawable 作为闪烁层
        GradientDrawable blinkDrawable = new GradientDrawable();
        blinkDrawable.setShape(GradientDrawable.RECTANGLE);

        // 设置闪烁的高亮颜色
        int highlightColor = MaterialColors.getColor(view, com.google.android.material.R.attr.colorSecondaryContainer);
        blinkDrawable.setColor(highlightColor);

        // 同步复杂的圆角值
        if (view instanceof MaterialCardView) {
            // 如果是 MaterialCardView，它拥有完美的 ShapeAppearanceModel
            MaterialCardView cardView = (MaterialCardView) view;
            ShapeAppearanceModel shapeModel = cardView.getShapeAppearanceModel();

            // 此时，我们需要将 ShapeModel 中的四个角（TopLeft, TopRight, BottomRight, BottomLeft）
            // 转换为 GradientDrawable 需要的 float[] 数组。
            // GradientDrawable.setCornerRadii 需要一个包含 8 个 float 值的数组，
            // 分别对应 [TLx, TLy, TRx, TRy, BRx, BRy, BLx, BLy]

            RectF rect = new RectF(0, 0, view.getWidth(), view.getHeight());
            float topLeft = shapeModel.getTopLeftCornerSize().getCornerSize(rect);
            float topRight = shapeModel.getTopRightCornerSize().getCornerSize(rect);
            float bottomRight = shapeModel.getBottomRightCornerSize().getCornerSize(rect);
            float bottomLeft = shapeModel.getBottomLeftCornerSize().getCornerSize(rect);

            float[] radii = new float[]{
                    topLeft, topLeft,      // Top Left: x, y
                    topRight, topRight,    // Top Right: x, y
                    bottomRight, bottomRight, // Bottom Right: x, y
                    bottomLeft, bottomLeft   // Bottom Left: x, y
            };
            blinkDrawable.setCornerRadii(radii);

        } else {
            // 兜底方案：如果 View 不是 MaterialCardView，尝试获取 View 本身的 Outline
            blinkDrawable.setCornerRadius(0); // 暂时设为 0，防止未知形状
        }

        // 将 Drawable 添加到 Overlay
        blinkDrawable.setAlpha(0); // 初始全透明
        view.getOverlay().add(blinkDrawable);

        // 设置 Bounds (必须，否则不显示)
        // 考虑到 RecyclerView 滚动，最好用 post 确保 View 测量完成
        view.post(() -> blinkDrawable.setBounds(0, 0, view.getWidth(), view.getHeight()));

        // 播放 Alpha 属性动画
        ObjectAnimator animator = ObjectAnimator.ofInt(
                blinkDrawable,
                "alpha",
                0,   // 全透明
                100         //不设置为255，为了避免完全遮罩内容
        );

        animator.setDuration(400); // 闪亮过程
        animator.setRepeatCount(1); // 呼吸 1 次
        animator.setRepeatMode(ObjectAnimator.REVERSE); // 倒序回弹

        // 清理工作
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // 必须移除，防止叠加和内存泄漏
                view.getOverlay().remove(blinkDrawable);
            }
        });

        animator.start();
    }
}
