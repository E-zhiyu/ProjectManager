package com.project.manager.services;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.project.manager.LogTags;
import com.project.manager.broadcast.NotificationAnalysisBroadcastReceiver;
import com.project.manager.broadcast.BroadcastConstants;
import com.project.manager.data.data_save.preference.AutoBookKeepingPreference;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.data.data_class.AnalysisRule;
import com.project.manager.data.data_class.running_account.RunningAccountBase;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;
import com.project.manager.data.data_class.Tag;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class AutoBookKeepingNotificationListenerService extends NotificationListenerService implements NotificationAnalysisBroadcastReceiver.BroadcastListener {
    private List<AnalysisRule> ruleList;                            //解析规则列表
    private NotificationAnalysisBroadcastReceiver ruleUpdateReceiver;    //规则更新的广播接收器
    private boolean isFunctionOpened;                               //通知解析功能是否开启

    @Override
    public void onCreate() {
        super.onCreate();

        //启动时则加载规则
        try {
            ruleList = AnalysisRule.loadAnalysisRule(getBaseContext());
        } catch (SQLiteException e) {
            ruleList = new ArrayList<>();
            ExceptionHelper.showExceptionDialog(getBaseContext(), e);
        }
        isFunctionOpened = AutoBookKeepingPreference.getNotificationAnalysisOpened(getBaseContext());   //启动时加载功能开关状态

        //注册规则更新和开关状态更新的广播接收器
        ruleUpdateReceiver = new NotificationAnalysisBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastConstants.ACTION_RULES_UPDATED.toString());       //过滤规则更新动作
        filter.addAction(BroadcastConstants.ACTION_NOTIFICATION_ANALYSIS_FUNCTION_SWITCHED.toString()); //过滤通知解析功能开关状态变化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(ruleUpdateReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(ruleUpdateReceiver, filter);
        }

        //发送通知监听服务已运行的通知
        sendBroadcast(new Intent(BroadcastConstants.ACTION_NOTIFICATION_LISTENER_ENABLED.toString()));
        Log.d(LogTags.NOTIFICATION_SERVICE.getV(), "通知监听服务已启动");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //注销广播接收器防止重复刷新UI
        if (ruleUpdateReceiver != null) {
            unregisterReceiver(ruleUpdateReceiver);
        }
    }

    @Override
    public void onNotificationPosted(@NonNull StatusBarNotification sbn) {
        if (!isFunctionOpened) return;  //功能未打开则直接结束

        //获取通知数据
        String packageName = sbn.getPackageName();
        String title = sbn.getNotification().extras.getString("android.title");
        String text = sbn.getNotification().extras.getString("android.text");
        if (text == null) return;

        Log.i(LogTags.NOTIFICATION_SERVICE.getV(), String.format("通知发送者包名：%s", packageName));
        Log.i(LogTags.NOTIFICATION_SERVICE.getV(), String.format("通知标题：%s", title));
        Log.i(LogTags.NOTIFICATION_SERVICE.getV(), String.format("通知内容：%s", text));

        // 处理通知内容
        for (AnalysisRule rule : ruleList) {
            String rule_package_name = rule.getPackageName();
            String rule_title = rule.getNotificationTitle();
            String rule_content = rule.getNotificationContent();

            Matcher matcher;            //匹配通知内容
            try {
                Pattern pattern = Pattern.compile(rule_content);    //编译为正则表达式
                matcher = pattern.matcher(text);
            } catch (PatternSyntaxException e) {
                Log.e(LogTags.NOTIFICATION_SERVICE.getV(), "正则表达式编译出错");
                Toast.makeText(
                        getBaseContext(),
                        String.format(
                                Locale.getDefault(),
                                "规则%s的正则表达式编译出错，请检查",
                                rule.getRuleName()),
                        Toast.LENGTH_SHORT
                ).show();
                continue;
            }

            if (rule_package_name.equals(packageName) && rule_title.equals(title) && matcher.find()) {
                Bundle dataBundle;
                try {
                    //获取标签编号
                    long rule_no = rule.getRuleNo();
                    long tag_no = Tag.getTagOfAnalysisRule(rule_no, getBaseContext()).getTno();

                    dataBundle = saveNewAccount(matcher, rule.getType(), tag_no, rule.getRuleName());
                    Log.d(LogTags.NOTIFICATION_SERVICE.getV(), "流水数据保存成功");
                } catch (SQLiteException e) {
                    Log.e(LogTags.NOTIFICATION_SERVICE.getV(), "流水数据保存失败或标签编号读取失败");
                    ExceptionHelper.showExceptionDialog(getBaseContext(), e);
                    return;
                } catch (NullPointerException e) {
                    Log.e(LogTags.NOTIFICATION_SERVICE.getV(), "通知解析失败");
                    ExceptionHelper.showExceptionDialog(getBaseContext(), e);
                    return;
                }

                //发送流水账记录增加的广播
                Intent accountAdded = new Intent(BroadcastConstants.ACTION_RUNNING_ACCOUNT_UPDATED.toString());
                accountAdded.putExtras(dataBundle);
                getBaseContext().sendBroadcast(accountAdded);

                break;  //匹配到规则则结束循环
            }
        }
    }

    /**
     * 处理规则更新的接口回调
     */
    @Override
    public void onRuleUpdated() {
        try {
            Log.d(LogTags.NOTIFICATION_SERVICE.getV(), "收到规则更新广播，正在更新规则……");
            ruleList = AnalysisRule.loadAnalysisRule(getBaseContext());
            Log.d(LogTags.NOTIFICATION_SERVICE.getV(), "规则更新成功");
        } catch (SQLiteException e) {
            Log.w(LogTags.NOTIFICATION_SERVICE.getV(), "规则更新失败");
            ExceptionHelper.showExceptionDialog(getBaseContext(), e);
        }
    }

    /**
     * 处理功能开关状态变更的接口回调
     */
    @Override
    public void onFunctionSwitched() {
        Log.d(LogTags.NOTIFICATION_SERVICE.getV(), "收到功能开关状态变更广播");
        isFunctionOpened = AutoBookKeepingPreference.getNotificationAnalysisOpened(getBaseContext());
        Log.d(LogTags.NOTIFICATION_SERVICE.getV(), "通知解析功能：" + isFunctionOpened);
    }

    /**
     * 解析并保存得到的流水记录
     *
     * @param matcher  正则表达式的匹配对象
     * @param type     解析规则中的流水种类
     * @param tag_no   解析规则对应的标签编号
     * @param ruleName 解析规则的名称
     * @return 解析通知内容后生成的流水数据包
     * @throws SQLiteException 流水数据保存失败引发的异常
     */
    @NonNull
    private Bundle saveNewAccount(@NonNull Matcher matcher, @NonNull RunningAccountType type, long tag_no, String ruleName) throws SQLiteException {
        //获取匹配到的金额数据
        double amount = Double.parseDouble(Objects.requireNonNull(matcher.group(1)));

        //获取当前的时间
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date now = new Date(currentTimeMillis);
        String time_str = dateFormat.format(now);

        //生成备注
        String remark = "自动记账：" + ruleName;

        //生成流水记录数据包
        Bundle dataBundle = new Bundle();
        dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);
        dataBundle.putString(KeyValueStrings.ACCOUNT_DATETIME.getValue(), time_str);
        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString());
        dataBundle.putDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), amount);
        dataBundle.putString(KeyValueStrings.ACCOUNT_REMARK.getValue(), remark);

        long rno = RunningAccountBase.saveNewAccount(dataBundle, getApplicationContext());
        dataBundle.putLong(KeyValueStrings.ACCOUNT_NO.getValue(), rno);

        return dataBundle;
    }
}
