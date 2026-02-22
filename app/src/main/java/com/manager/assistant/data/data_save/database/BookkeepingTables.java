package com.manager.assistant.data.data_save.database;

import androidx.annotation.NonNull;

public enum BookkeepingTables {
    BASIC("basic_data"),
    TRANSFER("transfer_data"),
    TAG("tag_data"),
    TAG_GROUP("tag_group_data"),
    ANALYSIS_RULE("analysis_rule_data"),
    RULE_ACCOUNT("rule_account_data"),
    PICTURE("picture"),
    BUDGET("budget"),
    BUDGET_TAG("budget_tag");

    private final String value;

    BookkeepingTables(String value) {
        this.value = value;
    }

    //重写toString方法以默认打印value属性
    @NonNull
    @Override
    public String toString() {
        return this.value;
    }
}
