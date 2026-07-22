package com.manager.assistant.auxiliary.enums;

public enum LogTags {
    AUTH_ACTIVITY("AuthActivity"),
    FULL_SCREEN_MEDIA_ACTIVITY("FullScreenMediaActivity"),
    MEDIA_HELPER("MediaHelper"),
    FILE_HELPER("FileHelper"),
    ACCOUNT_INPUT("RunningAccountInputActivity"),
    HOME_PAGE("HomePage"),
    NOTIFICATION_SERVICE("NotificationService"),
    DATA_IO_HELPER("DataIOHelper"),
    SETTING_FRAGMENT("SettingFragment"),
    BACKUP_WORKER("BackupWorker"),
    WORK_STATS("WorkStats"),
    PICTURE_HELPER("PictureHelper"),
    ACCOUNT_FRAGMENT("AccountFragment"),
    ACCOUNT_DATA_HELPER("AccountDataHelper"),
    SAF_HELPER("SAFHelper"),
    ZIP_HELPER("ZipHelper"),
    ALARM_HELPER("AlarmHelper"),
    DB("Database"),
    APPLICATION("Application"),
    BUDGET_RESET_RECEIVER("BudgetResetReceiver"),
    BOOT_RECEIVER("BootReceiver"),
    PERMISSION_HELPER("PermissionHelper"),
    BIOMETRIC_HELPER("BiometricHelper"),
    LIFECYCLE_MANAGER("LifecycleManager"),
    MAIN_ACTIVITY("MainActivity");
    private final String v;

    LogTags(String v) {
        this.v = v;
    }

    public String n() {
        return v;
    }
}
