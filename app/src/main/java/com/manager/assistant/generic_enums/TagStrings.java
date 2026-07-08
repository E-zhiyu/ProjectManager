package com.manager.assistant.generic_enums;

//用作视图Tag的枚举类
public enum TagStrings {
    MEDIA_SELECTION("media_selection"),         //媒体多选追踪器
    ACCOUNT_FRAGMENT("account_fragment"),       //流水记录输入Fragment
    TIME_PICKER("time_picker"),                 //时间选择弹窗
    DATE_PICKER("date_picker"),                 //日期选择弹窗
    TAG_SELECT_SHEET("tag_select_sheet"),       //标签选择弹窗
    TAG_MERGE_SHEET("tag_merge_sheet"),         //标签合并弹窗
    MEDIA_ADD_BOTTOM_SHEET("media_add_sheet");  //图片添加选项弹窗

    private final String value;

    TagStrings(String value) {
        this.value = value;
    }

    public String getTag() {
        return value;
    }
}
