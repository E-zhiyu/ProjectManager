package com.sly.coffer.auxiliary.enums.settings;

public enum BackupFrequency {
    MIN_15(0, 0, 0, "每15分钟", 1000 * 60 * 15),                //每15分钟
    DAY(0, 1, 1, "每天", 24L * 60 * 60 * 1000),                //每天
    WEEK(0, 2, 2, "每星期", 24L * 60 * 60 * 1000 * 7),          //每个星期
    MONTH(0, 3, 3, "每个月", 24L * 60 * 60 * 1000 * 7 * 30);    //每月
    private final int groupId;          //分组编号
    private final int itemId;           //选项编号
    private final int order;            //顺序
    private final String title;         //显示标题
    private final long intervalMillis;  //备份间隔时间(毫秒)

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

    BackupFrequency(int groupId, int itemId, int order, String title, long intervalMillis) {
        this.groupId = groupId;
        this.itemId = itemId;
        this.order = order;
        this.title = title;
        this.intervalMillis = intervalMillis;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }
}
