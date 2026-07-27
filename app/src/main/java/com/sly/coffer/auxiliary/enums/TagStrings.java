package com.sly.coffer.auxiliary.enums;

public enum TagStrings {
    BACKUP_WORKER("backup_worker"),             //自动备份的 Worker
    MEDIA_SELECTION("media_selection"),         //媒体多选追踪器
    TIME_PICKER("time_picker"),                 //时间选择弹窗
    DATE_PICKER("date_picker"),                 //日期选择弹窗
    ACCOUNT_FILTER_BOTTOM("account_filter_fragment"),   //流水记录输入Fragment
    TAG_SELECT_BOTTOM("tag_select_bottom"),     //标签选择弹窗
    MEDIA_ADD_BOTTOM("media_add_bottom");       //图片添加选项弹窗

    private final String value;

    TagStrings(String value) {
        this.value = value;
    }

    public String t() {
        return value;
    }
}
