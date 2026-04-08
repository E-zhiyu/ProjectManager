package com.manager.assistant.data.classes;

import androidx.annotation.Nullable;

import java.util.Objects;

public class TagGroup {
    private String groupName;       //标签组名称
    private final long groupNo;     //标签组编号

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getGroupNo() {
        return groupNo;
    }

    /**
     * 标签分组构造方法
     *
     * @param groupName 标签分组名
     * @param groupNo   标签分组编号
     */
    public TagGroup(String groupName, long groupNo) {
        this.groupName = groupName;
        this.groupNo = groupNo;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        TagGroup group = (TagGroup) obj;
        return group.getGroupNo() == groupNo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupNo);
    }
}
