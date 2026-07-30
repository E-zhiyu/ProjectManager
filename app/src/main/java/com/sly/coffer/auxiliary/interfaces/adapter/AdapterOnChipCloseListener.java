package com.sly.coffer.auxiliary.interfaces.adapter;

import android.view.View;

public interface AdapterOnChipCloseListener<T, A> {
    /**
     * Chip 关闭监听
     *
     * @param entity  被关闭的视图对应的数据实体
     * @param anchor  用于显示 PopupMenu 的锚点
     * @param adapter 适配器实例
     */
    void onClose(T entity, View anchor, A adapter);
}
