package com.manager.assistant.ui.others.listeners;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 控制组件显示或隐藏的RecyclerView的滚动监听器
 */
public abstract class RecyclerScrollHideShowListener extends RecyclerView.OnScrollListener {
    private static final int HIDE_THRESHOLD = 20; //滚动阈值，单位像素
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        //判断是否正在向上滚动（dy < 0表示向上滚动）
        if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
            onHide();
            controlsVisible = false;
            scrolledDistance = 0;
        }
        //判断是否正在向下滚动（dy > 0表示向下滚动）
        else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
            onShow();
            controlsVisible = true;
            scrolledDistance = 0;
        }

        //累计滚动距离，正数表示向下，负数表示向上
        if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            scrolledDistance += dy;
        }
    }

    public abstract void onHide();

    public abstract void onShow();
}
