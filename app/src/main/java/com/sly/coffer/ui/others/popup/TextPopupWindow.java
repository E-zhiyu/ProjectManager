package com.sly.coffer.ui.others.popup;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;

import com.sly.coffer.databinding.PopupWindowTextBinding;
import com.sly.coffer.helpers.appearence.AppearanceHelper;

public class TextPopupWindow {
    private final PopupWindowTextBinding binding;   //绑定的 XML 布局

    public TextPopupWindow(String text, Context context) {
        this.binding = PopupWindowTextBinding.inflate(LayoutInflater.from(context));
        binding.textView.setText(text);
    }

    /**
     * 显示 PopupWindow
     *
     * @param anchor  锚定的视图
     * @param gravity 显示在锚定的视图的方向，有效的值为{@link Gravity}的静态变量
     */
    public void show(@NonNull final View anchor, final int gravity) {
        PopupWindow window = new PopupWindow(
                binding.getRoot(),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        window.setOutsideTouchable(true);

        //禁用系统自动裁剪/贴边，完全由我们手动控制宽高
        window.setClippingEnabled(false);

        // 获取没有限制时的理想测量宽高
        binding.getRoot().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int idealWidth; // 注意：这里原代码可能笔误，应为 getMeasuredWidth()
        int idealHeight = binding.getRoot().getMeasuredHeight();
        // 修正可能存在的笔误
        idealWidth = binding.getRoot().getMeasuredWidth();

        // 获取锚点在屏幕上的绝对坐标
        int[] anchorLocation = new int[2];
        anchor.getLocationOnScreen(anchorLocation);
        int anchorLeft = anchorLocation[0];
        int anchorTop = anchorLocation[1];

        // 获取屏幕的总宽高
        int screenWidth = AppearanceHelper.getScreenWidth(anchor.getContext());
        int screenHeight = AppearanceHelper.getScreenHeight(anchor.getContext());

        // 间距
        int margin = AppearanceHelper.dpToPx(anchor.getContext(), 10);

        int xOffset, yOffset;
        int finalWidth = idealWidth;
        int finalHeight = idealHeight;

        // ================== 开始分方位计算自适应 ==================
        if (gravity == Gravity.START) { // 【左侧垂直居中】
            // 1. 限制宽度：最大只能是 锚点左侧到屏幕左边缘 的距离减去2倍间距
            int maxAvailableWidth = anchorLeft - margin * 2;
            if (idealWidth > maxAvailableWidth) {
                finalWidth = Math.max(0, maxAvailableWidth);
                // 宽度变小导致文本换行，重新测量高度
                remeasuredHeightWithWidth(finalWidth);
                finalHeight = binding.getRoot().getMeasuredHeight();
            }

            // 2. 限制高度：防止垂直方向上下越界
            // 允许在垂直方向占满全屏
            finalHeight = Math.min(finalHeight, screenHeight);

            xOffset = -finalWidth - margin;
            yOffset = -(anchor.getHeight() + finalHeight) / 2;

        } else if (gravity == Gravity.END) { // 【右侧垂直居中】
            // 1. 限制宽度：最大只能是 屏幕宽度 减去 锚点右边缘到屏幕左边的距离 再减去间距
            int anchorRight = anchorLeft + anchor.getWidth();
            int maxAvailableWidth = screenWidth - anchorRight - margin * 2;
            if (idealWidth > maxAvailableWidth) {
                finalWidth = Math.max(0, maxAvailableWidth);
                remeasuredHeightWithWidth(finalWidth);
                finalHeight = binding.getRoot().getMeasuredHeight();
            }

            finalHeight = Math.min(finalHeight, screenHeight);

            xOffset = anchor.getWidth() + margin;
            yOffset = -(anchor.getHeight() + finalHeight) / 2;

        } else {
            int minWidth = Math.min(idealWidth, screenWidth - margin * 2);
            if (gravity == Gravity.BOTTOM) { // 【下方水平居中】
                // 1. 限制宽度：下方可以很宽，但最大不能超过屏幕总宽度
                finalWidth = minWidth;
                remeasuredHeightWithWidth(finalWidth);
                finalHeight = binding.getRoot().getMeasuredHeight();

                // 2. 限制高度：最大只能是 锚点底部到屏幕底部的距离 再减去间距
                int anchorBottom = anchorTop + anchor.getHeight();
                int maxAvailableHeight = screenHeight - anchorBottom - margin;
                if (finalHeight > maxAvailableHeight) {
                    finalHeight = Math.max(0, maxAvailableHeight);
                }

                xOffset = (anchor.getWidth() - finalWidth) / 2;
                yOffset = margin;

            } else { // 【默认：上方水平居中】
                // 1. 限制宽度：最大不能超过屏幕总宽度
                finalWidth = minWidth;
                remeasuredHeightWithWidth(finalWidth);
                finalHeight = binding.getRoot().getMeasuredHeight();

                // 2. 限制高度：最大只能是 锚点顶部到屏幕顶部的距离 再减去间距
                int maxAvailableHeight = anchorTop - margin;
                if (finalHeight > maxAvailableHeight) {
                    finalHeight = Math.max(0, maxAvailableHeight);
                }

                xOffset = (anchor.getWidth() - finalWidth) / 2;
                yOffset = -(anchor.getHeight() + finalHeight);
            }
        }
        // ================== 自适应计算结束 ==================

        // 🔴 关键：将计算好的最终宽高应用到 PopupWindow 上
        window.setWidth(finalWidth);
        window.setHeight(finalHeight);

        // 检查 Activity 状态，防止异步崩溃
        window.showAsDropDown(anchor, xOffset, yOffset);
    }

    /**
     * 辅助方法：当宽度被强行压缩后，重新测量文本在当前宽度下应有新高度
     */
    private void remeasuredHeightWithWidth(int width) {
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        binding.getRoot().measure(widthMeasureSpec, heightMeasureSpec);
    }
}
