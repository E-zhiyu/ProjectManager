package com.project.manager.database;

import androidx.annotation.NonNull;

public enum BookKeepingColumns {
    RNO("Rno"),                 //流水编号
    AMOUNT("Amount"),           //金额
    TYPE("Type"),               //种类
    REMARK("Remark"),           //备注
    DATETIME("DateTime"),       //日期和时间
    EXPORT("Export"),           //转出账户
    IMPORT("Import"),           //转入账户
    TAG_NAME("TagName"),        //标签名称
    TAG_NO("TagNo"),            //标签编号
    GROUP_NAME("GroupName"),    //标签分组名称
    GROUP_NO("GroupNO"),        //标签分组编号
    RULE_NO("rule_no"),
    RULE_NAME("rule_name"),
    ACCOUNT_TYPE("account_type"),
    PACKAGE_NAME("package_name"),
    NOTIFICATION_TITLE("notification_title"),
    NOTIFICATION_CONTENT("notification_content");
    final String value;

    BookKeepingColumns(String value) {
        this.value = value;
    }

    //重写toString方法以默认打印value属性
    @NonNull
    @Override
    public String toString() {
        return this.value;
    }
}
