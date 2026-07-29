package com.sly.coffer.ui.others.scroller;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearSmoothScroller;

public class CustomOffsetSmoothScroller extends LinearSmoothScroller {
    private final int customOffsetPx;   //自定义的Offset值（像素）

    /**
     * 指定 Offset 的自定义 Offset 平滑滚动器的构造方法
     *
     * @param context        上下文
     * @param customOffsetPx 目标 Item 顶端距离 RecyclerView 顶端的固定像素距离
     */
    public CustomOffsetSmoothScroller(Context context, int customOffsetPx) {
        super(context);
        this.customOffsetPx = customOffsetPx;
    }

    @Override
    protected int getVerticalSnapPreference() {
        // 先设定基础策略为靠顶对齐
        return SNAP_TO_START;
    }

    @Override
    public int calculateDyToMakeVisible(View view, int snapPreference) {
        // super 计算出来的是刚好置顶需要的滑行距离（负数或正数）
        int dy = super.calculateDyToMakeVisible(view, snapPreference);

        // 加上我们自定义的偏移量
        return dy + customOffsetPx;
    }
}