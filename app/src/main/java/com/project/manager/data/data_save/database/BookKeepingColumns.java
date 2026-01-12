package com.project.manager.data.data_save.database;

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
    RULE_NO("Rule_no"),         //规则编号
    RULE_NAME("Rule_name"),     //规则名称
    PACKAGE_NAME("Package_name"),   //应用包名
    NOTIFICATION_TITLE("Notification_title"),   //通知标题
    NOTIFICATION_CONTENT("Notification_content"),   //通知内容（正则表达式）
    PICTURE_URI("picture_uri"), //图片文件Uri
    PNO("pno");                 //图片编号
    private final String value;

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
