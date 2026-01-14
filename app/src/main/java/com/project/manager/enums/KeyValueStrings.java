package com.project.manager.enums;

//用作关键字的字符串枚举
public enum KeyValueStrings {
    RNO("rno"),                                             //流水编号
    ACCOUNT_TYPE("account_type"),                           //流水种类
    ACCOUNT_REMARK("account_remark"),                       //流水备注
    ACCOUNT_IS_DEFAULT_REMARK("account_isDefaultRemark"),   //是否使用默认备注
    ACCOUNT_DATETIME("account_datetime"),                   //流水日期和时间
    ACCOUNT_AMOUNT("account_amount"),                       //流水金额
    ACCOUNT_NO("account_no"),                               //流水编号
    VIEW_HOLDER_POSITION("account_view_position"),          //流水视图在列表中的索引值
    ACCOUNT_EXPORT("account_export"),                       //流水转出账户
    ACCOUNT_IMPORT("account_import"),                       //流水转入账户
    IS_MODIFY_MODE("is_modify_mode"),                       //是否为修改模式
    TAG_NAME("tag_name"),                                   //标签名称
    TAG_NO("tag_no"),                                       //标签编号
    TAG_GROUP_NAME("tag_group_name"),                       //标签分组名称
    TAG_GROUP_NO("tag_group_no"),                           //标签分组编号
    MERGE_TARGET_NO("merged_no"),                           //合并到的分组或标签编号
    TAG_GROUP_NAME_LIST("tag_group_name_list"),             //标签组名称列表
    ANALYSIS_RULE_NAME("analysis_rule_name"),               //通知解析规则名称
    ANALYSIS_RULE_NO("analysis_rule_no"),                   //通知解析规则编号
    PACKAGE_NAME("package_name"),                           //包名
    NOTIFICATION_TITLE("notification_title"),               //通知标题
    NOTIFICATION_CONTENT("notification_content"),           //通知内容
    FILE_URI("file_uri");                                   //文件Uri

    final String value;

    KeyValueStrings(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
