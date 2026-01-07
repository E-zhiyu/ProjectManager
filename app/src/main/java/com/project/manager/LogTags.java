package com.project.manager;

public enum LogTags {
    NOTIFICATION_SERVICE("通知监听服务"),
    RULE_UPDATE_RECEIVER("规则更新广播接收器"),
    FILE_IO_HELPER("文件输入输出帮助器"),
    SETTING_FRAGMENT("设置界面"),
    BACKUP_WORKER("自动备份Worker"),
    WORK_STATS("任务状态"),
    CAMERA_ACTIVITY("相机界面");
    private final String v;

    LogTags(String v) {
        this.v = v;
    }

    public String getV() {
        return v;
    }
}
