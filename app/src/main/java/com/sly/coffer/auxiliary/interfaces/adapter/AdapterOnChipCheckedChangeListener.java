package com.sly.coffer.auxiliary.interfaces.adapter;

import android.view.View;

public interface AdapterOnChipCheckedChangeListener<T> {
    /**
     * Chip 选择状态变更监听
     *
     * @param entity    被关闭的视图对应的数据实体
     * @param anchor    用于显示 PopupMenu 的锚点
     * @param isChecked 是否选中
     */
    void onCheckedChange(T entity, boolean isChecked, View anchor);
}
