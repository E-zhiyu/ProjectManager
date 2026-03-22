package com.manager.assistant.generic_enums;

import android.app.NotificationManager;

public enum ChannelInfo {
    BUDGET_AMOUNT(
            "budget_amount_channel",
            "预算提醒",
            "当预算余额过低时发送通知提醒",
            NotificationManager.IMPORTANCE_HIGH
    ),
    AUTO_BOOKKEEPING(
            "auto_bookkeeping_channel",
            "自动记账",
            "触发自动记账后发送用于确认或修改信息的通知",
            NotificationManager.IMPORTANCE_HIGH
    );

    private final String id;            //通道标识符
    private final String name;          //通道名称
    private final String description;   //通道描述
    private final int importance;       //通知重要性

    ChannelInfo(String id, String name, String description, int importance) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.importance = importance;
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
}
