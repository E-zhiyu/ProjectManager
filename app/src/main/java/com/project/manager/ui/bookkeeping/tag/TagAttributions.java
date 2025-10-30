package com.project.manager.ui.bookkeeping.tag;

public enum TagAttributions {
    NAME("tag_name"),           //标签名称
    TAG_NO("tag_no"),           //标签编号
    GROUP_NAME("group_name"),   //标签分组名称
    GROUP_NO("group_no");       //标签分组编号

    final String value;

    TagAttributions(String value) {
        this.value = value;
    }
}
