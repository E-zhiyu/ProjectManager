package com.project.manager.database;

import androidx.annotation.NonNull;

public enum RunningAccountTables {
    BASIC("basic_data"),
    TRANSFER("transfer_data"),
    TAG("tag_data"),
    TAG_GROUP("tag_group_data");

    final String value;

    RunningAccountTables(String value) {
        this.value = value;
    }

    //重写toString方法以默认打印value属性
    @NonNull
    @Override
    public String toString() {
        return this.value;
    }
}
