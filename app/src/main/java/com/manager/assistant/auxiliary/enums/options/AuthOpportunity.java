package com.manager.assistant.auxiliary.enums.options;

public enum AuthOpportunity {
    EVERY_TIME(0, 0, 0, 0, "每次"),
    SECOND_10(0, 1, 1, 10, "间隔10秒"),
    SECOND_30(0, 2, 2, 30, "间隔30秒"),
    SECOND_60(0, 3, 3, 60, "间隔60秒");
    private final int groupId;  //分组编号
    private final int itemId;   //选项编号
    private final int order;    //顺序
    private final int second;   //间隔秒数
    private final String title; //显示标题

    AuthOpportunity(int groupId, int itemId, int order, int second, String title) {
        this.groupId = groupId;
        this.itemId = itemId;
        this.order = order;
        this.second = second;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /**
     * 获取间隔时间
     *
     * @return 间隔时间时间戳
     */
    public long getTimeMilli() {
        return second * 1000L;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getOrder() {
        return order;
    }
}
