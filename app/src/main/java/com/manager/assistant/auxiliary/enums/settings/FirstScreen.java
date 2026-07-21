package com.manager.assistant.auxiliary.enums.settings;

public enum FirstScreen {
    BOOKKEEPING(0,0,0,"记账"),
    HOME(0,1,1,"主页");
    private final int groupId;  //分组编号
    private final int itemId;   //选项编号
    private final int order;    //顺序
    private final String title; //显示标题

    FirstScreen(int groupId, int itemId, int order, String title) {
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
