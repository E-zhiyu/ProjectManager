package com.sly.coffer.ui.pages.main.settings.sub.media;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sly.coffer.R;

public enum Ordering {
    TIME(R.string.order_by_time),   //时间排序
    SIZE(R.string.order_by_size),   //大小排序
    NAME(R.string.order_by_name);   //名称排序

    private final int strRes;

    Ordering(int strRes) {
        this.strRes = strRes;
    }

    @NonNull
    public String getTitle(@NonNull Context context) {
        return context.getString(strRes);
    }
}
