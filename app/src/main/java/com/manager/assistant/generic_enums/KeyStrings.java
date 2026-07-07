package com.manager.assistant.generic_enums;

//用作关键字的字符串枚举
public enum KeyStrings {
    ACCOUNT_TYPE("account_type"),                           //流水种类
    ACCOUNT_REMARK("account_remark"),                       //流水备注
    ACCOUNT_OLD_DATETIME("account_old_datetime"),           //流水记录修改前的日期和时间
    ACCOUNT_DATETIME("account_datetime"),                   //流水日期和时间
    ACCOUNT_AMOUNT("account_amount"),                       //流水金额
    ACCOUNT_ID("account_id"),                               //流水编号
    VIEW_HOLDER_POSITION("view_position"),                  //视图在列表中的索引值
    ACCOUNT_EXPORT("account_export"),                       //流水转出账户
    ACCOUNT_IMPORT("account_import"),                       //流水转入账户
    IS_MODIFY_MODE("is_modify_mode"),                       //是否为修改模式
    TAG_NAME("tag_name"),                                   //标签名称
    TAG_NO("tag_no"),                                       //标签编号
    TAG_SCOPE("tag_scope"),                                 //标签作用域
    TAG_GROUP_NAME("tag_group_name"),                       //标签分组名称
    TAG_GROUP_NO("tag_group_no"),                           //标签分组编号
    TAG_GROUP_NO_NEW("tag_group_no_new"),                   //标签输入界面修改分组后的新的分组编号
    MERGE_TARGET_NO("merged_no"),                           //合并到的分组或标签编号
    ANALYSIS_RULE_NAME("analysis_rule_name"),               //通知解析规则名称
    ANALYSIS_RULE_NO("analysis_rule_no"),                   //通知解析规则编号
    PACKAGE_NAME("package_name"),                           //包名
    NOTIFICATION_TITLE("notification_title"),               //通知标题
    NOTIFICATION_CONTENT("notification_content"),           //通知内容
    FILE_URI("file_uri"),                                   //文件Uri
    BUDGET_NAME("budget_name"),                             //预算名称
    INIT_AMOUNT("init_amount"),                             //预算初始金额
    LEFT_AMOUNT("left_amount"),                             //预算剩余金额
    START_DATE("start_date"),                               //预算起算日期
    BNO("bno"),                                             //预算编号
    BUDGET_RESET_FREQUENCY("budget_reset_frequency"),       //预算重置频率
    NOTIFICATION_ID("notification_id");                     //应用通知ID，用于区分不同的通知

    final String value;

    KeyStrings(String value) {
        this.value = value;
    }

    public String v() {
        return value;
    }
}
