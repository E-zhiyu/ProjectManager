package com.manager.assistant.generic_enums;

import android.app.NotificationManager;

public enum ChannelInfo {
    BUDGET_AMOUNT(
            "budget_amount_channel",
            "预算余额提醒通道",
            "当预算余额过低时发送通知提醒",
            NotificationManager.IMPORTANCE_DEFAULT,
            true
    );

    private final String id;            //通道标识符
    private final String name;          //通道名称
    private final String description;   //通道描述
    private final int importance;       //通知重要性
    private final boolean showBadge;    //通知是否显示角标

    ChannelInfo(String id, String name, String description, int importance, boolean showBadge) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.importance = importance;
        this.showBadge = showBadge;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getImportance() {
        return importance;
    }

    public boolean isShowBadge() {
        return showBadge;
    }
}
