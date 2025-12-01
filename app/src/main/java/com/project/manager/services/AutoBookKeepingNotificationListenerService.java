package com.project.manager.services;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.annotation.NonNull;

public class AutoBookKeepingNotificationListenerService extends NotificationListenerService {
    @Override
    public void onNotificationPosted(@NonNull StatusBarNotification sbn) {
        // 当有新通知时调用
        String packageName = sbn.getPackageName();
        String title = sbn.getNotification().extras.getString("android.title");
        String text = sbn.getNotification().extras.getString("android.text");

        // 处理通知内容
        // 注意：这里只是示例，实际使用时应该考虑权限和隐私问题

    }
}
