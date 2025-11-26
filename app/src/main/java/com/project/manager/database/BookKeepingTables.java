package com.project.manager.database;

import androidx.annotation.NonNull;

public enum BookKeepingTables {
    BASIC("basic_data"),
    TRANSFER("transfer_data"),
    TAG("tag_data"),
    TAG_GROUP("tag_group_data"),
    NOTIFICATION_ANALYSIS_RULE("notification_analysis_rule_data");

    final String value;

    BookKeepingTables(String value) {
        this.value = value;
    }

    //重写toString方法以默认打印value属性
    @NonNull
    @Override
    public String toString() {
        return this.value;
    }
}
