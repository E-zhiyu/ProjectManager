package com.manager.assistant.auxiliary.interfaces.adapter;

import android.view.View;

public interface AdapterOnLongClickListener<T> {
    /**
     * 长按回调
     *
     * @param entity 被长按的视图对应的数据实体
     * @param anchor 用于显示 PopupMenu 的锚点
     */
    void onLongClick(T entity, View anchor);
}
