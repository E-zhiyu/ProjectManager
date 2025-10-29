package com.project.manager.ui.bookkeeping.tag;

public enum TagAttributions {
    NAME("tag_name"),
    TAG_NO("tag_no"),
    GROUP_NAME("group_name"),
    GROUP_NO("group_no");

    final String value;

    TagAttributions(String value) {
        this.value = value;
    }
}
