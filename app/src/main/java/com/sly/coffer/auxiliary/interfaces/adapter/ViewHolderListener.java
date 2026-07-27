package com.sly.coffer.auxiliary.interfaces.adapter;

import android.view.View;

public interface ViewHolderListener {
    /**
     * ViewHolder 被单击的回调，用于向适配器传递位置数据
     *
     * @param pos    被点击的 ViewHolder 在适配器中的位置
     * @param anchor 显示 PopupMenu 的锚点
     */
    void onClick(int pos, View anchor);

    /**
     * ViewHolder 被长按的回调，用于向适配器传递位置数据
     *
     * @param pos    被点击的 ViewHolder 在适配器中的位置
     * @param anchor 显示 PopupMenu 的锚点
     */
    void onLongClick(int pos, View anchor);
}
