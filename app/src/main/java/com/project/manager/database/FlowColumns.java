package com.project.manager.database;

public enum FlowColumns {
    FNO("Fno"),
    AMOUNT("Amount"),
    TYPE("Type"),
    REMARK("Remark"),
    DATETIME("DateTime"),
    EXPORT("Export"),
    IMPORT("Import"),
    TAG_NAME("TagName"),
    TAG_NO("TagNo"),
    GROUP_NAME("GroupName"),
    GROUP_NO("GroupNO");
    final String value;

    FlowColumns(String value) {
        this.value = value;
    }

    //重写toString方法以默认打印value属性
    @Override
    public String toString() {
        return this.value;
    }
}
