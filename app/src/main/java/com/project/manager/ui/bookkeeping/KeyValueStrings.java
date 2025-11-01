package com.project.manager.ui.bookkeeping;

//用作键的字符串枚举
public enum KeyValueStrings {
    FLOW_NAME("flow_name"),                     //流水名称
    FLOW_TYPE("flow_type"),                     //流水种类
    FLOW_REMARK("flow_remark"),                 //流水备注
    FLOW_DATETIME("flow_datetime"),             //流水日期和时间
    FLOW_AMOUNT("flow_amount"),                 //流水金额
    FLOW_NO("flow_no"),                         //流水编号
    FLOW_VIEW_POSITION("flow_view_position"),   //流水视图在列表中的索引值
    FLOW_EXPORT("flow_export"),                 //流水转出账户
    FLOW_IMPORT("flow_import"),                 //流水转入账户
    TAG_NAME("tag_name"),                       //标签名称
    TAG_NO("tag_no"),                           //标签编号
    TAG_GROUP_NAME("tag_group_name"),           //标签分组名称
    TAG_GROUP_NO("tag_group_no"),               //标签分组编号
    TAG_GROUP_NAME_LIST("tag_group_name_list"); //标签组名称列表

    final String value;

    KeyValueStrings(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
