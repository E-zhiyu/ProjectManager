package com.project.manager;

public enum LogTags {
    NOTIFICATION_SERVICE("通知监听服务");
    private final String v;

    LogTags(String v) {
        this.v = v;
    }

    public String getV() {
        return v;
    }
}
