package com.sly.coffer.ui.others.decoration.sticky;

import android.content.Context;

/**
 * 粘性头部适配器
 * @param <T> 粘性头部显示的数据类型
 */
public interface StickyHeaderAdapter<T> {
    /**
     * 判断指定的位置是否为粘性头部视图
     *
     * @param position 视图的位置
     * @return 是否为粘性头部视图
     */
    boolean isHeader(int position);

    /**
     * 获取对应位置下粘性头部视图的文本
     *
     * @param position 视图位置
     * @return 粘性头部视图的文本
     */
    T getHeaderData(int position, Context context);
}
