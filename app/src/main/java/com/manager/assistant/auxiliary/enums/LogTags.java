package com.manager.assistant.auxiliary.enums;

public enum LogTags {
    AUTH_ACTIVITY("AuthActivity"),
    FULL_SCREEN_MEDIA_ACTIVITY("FullScreenMediaActivity"),
    FILE_HELPER("FileHelper"),
    ACCOUNT_INPUT("RunningAccountInputActivity"),
    NOTIFICATION_SERVICE("NotificationService"),
    DATA_IO_HELPER("DataIOHelper"),
    BACKUP_WORKER("BackupWorker"),
    WORK_STATS("WorkStats"),
    ACCOUNT_FRAGMENT("AccountFragment"),
    SAF_HELPER("SAFHelper"),
    ZIP_HELPER("ZipHelper"),
    ALARM_HELPER("AlarmHelper"),
    APPLICATION("Application"),
    BUDGET_RESET_RECEIVER("BudgetResetReceiver"),
    BOOT_RECEIVER("BootReceiver"),
    PERMISSION_HELPER("PermissionHelper"),
    BIOMETRIC_HELPER("BiometricHelper");
    private final String v;

    LogTags(String v) {
        this.v = v;
    }

    public String n() {
        return v;
    }
}
