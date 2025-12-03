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

import com.project.manager.LogTags;
import com.project.manager.broadcast.AutoBookKeepingBroadcastReceiver;
import com.project.manager.broadcast.BroadcastConstants;
import com.project.manager.preference.AutoBookKeepingPreference;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis.AnalysisRule;
import com.project.manager.ui.bookkeeping.running_account_edit.RunningAccountBase;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;

import java.util.List;

public class AutoBookKeepingNotificationListenerService extends NotificationListenerService implements AutoBookKeepingBroadcastReceiver.BroadcastListener {
    private List<AnalysisRule> ruleList;                            //解析规则列表
    private AutoBookKeepingBroadcastReceiver ruleUpdateReceiver;    //规则更新的广播接收器
    private boolean isFunctionOpened;                               //通知解析功能是否开启

    @Override
    public void onCreate() {
        super.onCreate();

        ruleList = AnalysisRule.loadAnalysisRule(getBaseContext()); //启动则加载规则
        isFunctionOpened = AutoBookKeepingPreference.getNotificationAnalysisOpened(getBaseContext());   //启动时加载功能开关状态

        //注册规则更新的广播接收器
        ruleUpdateReceiver = new AutoBookKeepingBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastConstants.ACTION_RULES_UPDATED.toString());       //过滤规则更新动作
        filter.addAction(BroadcastConstants.ACTION_NOTIFICATION_ANALYSIS_FUNCTION_SWITCHED.toString()); //过滤通知解析功能开关状态变化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(ruleUpdateReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(ruleUpdateReceiver, filter);
        }
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
        //获取通知数据
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
            if (isFunctionOpened && rule_package_name.equals(packageName) && rule_title.equals(title)) {
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

    @Override
    public void onRuleUpdated() {
        try {
            Log.d(LogTags.NOTIFICATION_SERVICE.getV(), "收到规则更新广播，正在更新规则……");
            ruleList = AnalysisRule.loadAnalysisRule(getBaseContext());
            Log.d(LogTags.NOTIFICATION_SERVICE.getV(), "规则更新成功");
        } catch (SQLiteException e) {
            Log.w(LogTags.NOTIFICATION_SERVICE.getV(), "规则更新失败");
        }
    }

    @Override
    public void onFunctionSwitched() {
        Log.d(LogTags.NOTIFICATION_SERVICE.getV(), "收到功能开关状态变更广播");
        isFunctionOpened = AutoBookKeepingPreference.getNotificationAnalysisOpened(getBaseContext());
        Log.d(LogTags.NOTIFICATION_SERVICE.getV(), "通知解析功能：" + isFunctionOpened);
    }
}
