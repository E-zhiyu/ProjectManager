package com.sly.coffer.auxiliary.enums;

public enum LogTags {
    AUTH_ACTIVITY("AuthActivity"),
    FULL_SCREEN_MEDIA_ACTIVITY("FullScreenMediaActivity"),
    FILE_HELPER("FileHelper"),
    ACCOUNT_INPUT("RunningAccountInputActivity"),
    AB_NOTIFICATION_LISTENER_SERVICE("AbNotificationListenerService"),
    PICK_ACCESSIBILITY_SERVICE("PickAccessibilityService"),
    AB_ACCESSIBILITY_SERVICE("AbAccessibilityService"),
    DATA_IO_HELPER("DataIOHelper"),
    BACKUP_WORKER("BackupWorker"),
    WORK_STATS("WorkStats"),
    ACCOUNT_FRAGMENT("AccountFragment"),
    SAF_HELPER("SAFHelper"),
    SCROLL_HELPER("ScrollHelper"),
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
