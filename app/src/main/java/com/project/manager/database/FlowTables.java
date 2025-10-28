package com.project.manager.database;

public enum FlowTables {
    BASIC("basic_data"),
    TRANSFER("transfer_data"),
    TAG("tag_data"),
    TAG_GROUP("tag_group_data");

    final String value;

    FlowTables(String value) {
        this.value = value;
    }

    //重写toString方法以默认打印value属性
    @Override
    public String toString() {
        return this.value;
    }
}
