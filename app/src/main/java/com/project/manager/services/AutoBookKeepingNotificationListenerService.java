package com.project.manager.services;

import android.content.Context;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.project.manager.LogTags;
import com.project.manager.broadcast.AnalysisRuleUpdateReceiver;
import com.project.manager.broadcast.BroadcastConstants;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.AnalysisRule;
import com.project.manager.ui.bookkeeping.running_account_edit.RunningAccountBase;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;

import java.util.List;

public class AutoBookKeepingNotificationListenerService extends NotificationListenerService {
    private List<AnalysisRule> ruleList;                        //解析规则列表
    private AnalysisRuleUpdateReceiver ruleUpdateReceiver;      //规则更新的广播接收器

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onCreate() {
        super.onCreate();

        ruleList = AnalysisRule.loadAnalysisRule(getBaseContext()); //启动则加载规则

        //注册规则更新的广播接收器
        ruleUpdateReceiver = new AnalysisRuleUpdateReceiver(this::loadRules);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastConstants.ACTION_RULES_UPDATED.toString());
        registerReceiver(ruleUpdateReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ruleUpdateReceiver != null) {
            unregisterReceiver(ruleUpdateReceiver);
        }
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
        for (AnalysisRule rule : ruleList) {
            String rule_package_name = rule.getPackageName();
            String rule_title = rule.getNotificationTitle();
            String rule_content = rule.getNotificationContent();

            //TODO:完善正则表达式匹配内容的逻辑
            if (rule_package_name.equals(packageName) && rule_title.equals(title)) {
                //TODO: 完善正则表达式解析数据的逻辑
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

    private void loadRules() {
        try {
            Log.d(LogTags.NOTIFICATION_SERVICE.getV(), "收到规则更新广播，正在更新规则");
            ruleList = AnalysisRule.loadAnalysisRule(getBaseContext());
            Log.d(LogTags.NOTIFICATION_SERVICE.getV(), "规则更新成功");
        } catch (SQLiteException e) {
            Log.w(LogTags.NOTIFICATION_SERVICE.getV(), "规则更新失败");
        }
    }
}
