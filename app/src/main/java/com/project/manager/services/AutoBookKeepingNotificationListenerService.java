package com.project.manager.services;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.NonNull;

import com.project.manager.LogTags;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.running_account_edit.RunningAccountBase;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;

public class AutoBookKeepingNotificationListenerService extends NotificationListenerService {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onNotificationPosted(@NonNull StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        String title = sbn.getNotification().extras.getString("android.title");
        String text = sbn.getNotification().extras.getString("android.text");

        Log.i(LogTags.NOTIFICATION_SERVICE.getV(), String.format("通知发送者包名：%s", packageName));
        Log.i(LogTags.NOTIFICATION_SERVICE.getV(), String.format("通知标题：%s", title));
        Log.i(LogTags.NOTIFICATION_SERVICE.getV(), String.format("通知内容：%s", text));

        // 处理通知内容
        if (packageName.equals("com.notification.sender")) {
            Bundle dataBundle = new Bundle();
            dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), 0);
            dataBundle.putString(KeyValueStrings.ACCOUNT_DATETIME.getValue(), "2025-12-01 08:00");
            dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), RunningAccountType.EXPENSE.toString());
            dataBundle.putDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), 11.0);
            dataBundle.putString(KeyValueStrings.ACCOUNT_REMARK.getValue(), "自动记账测试");
            RunningAccountBase.saveNewAccount(dataBundle, getApplicationContext());
        }
    }
}
