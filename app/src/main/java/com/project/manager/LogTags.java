package com.project.manager;

public enum LogTags {
    NOTIFICATION_SERVICE("通知监听服务"),
    RULE_UPDATE_RECEIVER("规则更新广播接收器");
    private final String v;

    LogTags(String v) {
        this.v = v;
    }

    public String getV() {
        return v;
    }
}
