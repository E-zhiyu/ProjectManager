package com.manager.assistant.auxiliary.enums.settings;

public enum NotificationClickBehaviour {
    NONE(0,0,0,"无"),
    KEEP(0, 1, 1, "保留记录"),
    ABADON(0, 2, 2, "舍弃记录");
    private final int groupId;  //分组编号
    private final int itemId;   //选项编号
    private final int order;    //顺序
    private final String title; //显示标题

    NotificationClickBehaviour(int groupId, int itemId, int order, String title) {
        this.groupId = groupId;
        this.itemId = itemId;
        this.order = order;
        this.title = title;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public int getOrder() {
        return order;
    }
}
