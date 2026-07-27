package com.sly.coffer.auxiliary.interfaces.adapter;

import android.view.View;

public interface AdapterOnCheckedChangeListener<T> {
    /**
     *
     * @param entity    变化的实体数据
     * @param finalStat 变化后是否被选中
     * @param anchor    显示 PopupMenu 的锚点
     */
    void onCheckedChange(T entity, boolean finalStat, View anchor);
}
