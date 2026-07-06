package com.manager.assistant.auxiliary.interfaces.adapter;

import android.view.View;

public interface AdapterOnClickListener<T> {
    /**
     * 单击监听
     *
     * @param entity 被点击的视图对应的数据实体
     * @param anchor 用于显示 PopupMenu 的锚点
     */
    void onClick(T entity, View anchor);
}
