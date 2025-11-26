package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis;

import android.content.Context;

import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;

import java.util.ArrayList;
import java.util.List;

public class AnalysisRule {
    private String ruleName;                //规则名称
    private long ruleNo;                    //规则编号
    private RunningAccountType accountType; //流水种类
    private long tagNo;                     //流水标签编号
    private String packageName;             //包名
    private String notificationTitle;       //通知标题
    private String notificationContent;     //通知内容

    public AnalysisRule(String ruleName, long ruleNo, RunningAccountType accountType, long tagNo, String packageName, String notificationTitle, String notificationContent) {
        this.ruleName = ruleName;
        this.ruleNo = ruleNo;
        this.accountType = accountType;
        this.tagNo = tagNo;
        this.packageName = packageName;
        this.notificationTitle = notificationTitle;
        this.notificationContent = notificationContent;
    }

    public String getRuleName() {
        return ruleName;
    }

    public long getRuleNo() {
        return ruleNo;
    }

    public RunningAccountType getAccountType() {
        return accountType;
    }

    public long getTagNo() {
        return tagNo;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public String getNotificationContent() {
        return notificationContent;
    }

    public static List<AnalysisRule> loadAnalysisRule(Context context) {
        //TODO: 完成解析规则读取方法
        return new ArrayList<>();
    }
}
