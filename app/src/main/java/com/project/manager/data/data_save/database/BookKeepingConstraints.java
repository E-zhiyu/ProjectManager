package com.project.manager.data.data_save.database;

import androidx.annotation.NonNull;

public enum BookKeepingConstraints {
    FK_TAG_NO("fk_tag_no"),         //标签编号外键约束
    FK_RNO("fk_rno"),               //流水账编号外键约束
    FK_GROUP_NO("fk_group_no");     //分组编号外键约束

    private final String value;

    BookKeepingConstraints(String value) {
        this.value = value;
    }

    //重写toString方法以默认打印value属性
    @NonNull
    @Override
    public String toString() {
        return this.value;
    }
}
