package com.project.manager;

public enum LogTags {
    NOTIFICATION_SERVICE("通知监听服务"),
    RULE_UPDATE_RECEIVER("规则更新广播接收器"),
    SAF_FILE_HELPER("SAF文件帮助器"),
    SETTING_FRAGMENT("设置界面");
    private final String v;

    LogTags(String v) {
        this.v = v;
    }

    public String getV() {
        return v;
    }
}
