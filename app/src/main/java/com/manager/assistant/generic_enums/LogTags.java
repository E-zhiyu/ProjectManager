package com.manager.assistant.generic_enums;

public enum LogTags {
    HOME_PAGE("HomePage"),
    NOTIFICATION_SERVICE("NotificationService"),
    RULE_UPDATE_RECEIVER("RuleUpdateReceiver"),
    DATA_IO_HELPER("DataIOHelper"),
    SETTING_FRAGMENT("SettingFragment"),
    BACKUP_WORKER("BackupWorker"),
    WORK_STATS("WorkStats"),
    CAMERA_ACTIVITY("CameraActivity"),
    PICTURE_HELPER("PictureHelper"),
    ACCOUNT_FRAGMENT("AccountFragment"),
    PICTURE_ADAPTER("PictureAdapter"),
    ACCOUNT_DATA_HELPER("AccountDataHelper"),
    DB("Database"),
    APPLICATION("Application"),
    ACCOUNT_ADAPTER("AccountRecyclerAdapter"),
    BUDGET_RESET_RECEIVER("BudgetResetReceiver"),
    BOOT_RECEIVER("BootReceiver"),
    PERMISSION_HELPER("PermissionHelper"),
    BIOMETRIC_HELPER("BiometricHelper"),
    LIFECYCLE_MANAGER("LifecycleManager");
    private final String v;

    LogTags(String v) {
        this.v = v;
    }

    public String getV() {
        return v;
    }
}
