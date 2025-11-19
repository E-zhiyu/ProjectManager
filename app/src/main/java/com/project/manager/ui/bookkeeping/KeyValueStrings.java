package com.project.manager.ui.bookkeeping;

//用作关键字的字符串枚举
public enum KeyValueStrings {
    ACCOUNT_NAME("account_name"),                           //流水名称
    ACCOUNT_TYPE("account_type"),                           //流水种类
    ACCOUNT_REMARK("account_remark"),                       //流水备注
    ACCOUNT_IS_DEFAULT_REMARK("account_isDefaultRemark"),   //是否使用默认备注
    ACCOUNT_DATETIME("account_datetime"),                   //流水日期和时间
    ACCOUNT_AMOUNT("account_amount"),                       //流水金额
    ACCOUNT_NO("account_no"),                               //流水编号
    ACCOUNT_VIEW_POSITION("account_view_position"),         //流水视图在列表中的索引值
    ACCOUNT_EXPORT("account_export"),                       //流水转出账户
    ACCOUNT_IMPORT("account_import"),                       //流水转入账户
    TAG_NAME("tag_name"),                                   //标签名称
    TAG_NO("tag_no"),                                       //标签编号
    TAG_GROUP_NAME("tag_group_name"),                       //标签分组名称
    TAG_GROUP_NO("tag_group_no"),                           //标签分组编号
    MERGE_TARGET_NO("merged_no"),                           //合并到的分组或标签编号
    TAG_GROUP_NAME_LIST("tag_group_name_list");             //标签组名称列表

    final String value;

    KeyValueStrings(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
