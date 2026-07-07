package com.manager.assistant.data.classes;

import com.manager.assistant.auxiliary.enums.AccountType;

public class AnalysisRule {
    private final String ruleName;                //规则名称
    private final long ruleNo;                    //规则编号
    private final AccountType type;        //流水种类
    private final String packageName;             //包名
    private final String notificationTitle;       //通知标题
    private final String notificationContent;     //通知内容

    public AnalysisRule(String ruleName, long ruleNo, AccountType accountType, String packageName, String notificationTitle, String notificationContent) {
        this.ruleName = ruleName;
        this.ruleNo = ruleNo;
        this.type = accountType;
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

    public AccountType getType() {
        return type;
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
}
