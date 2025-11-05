package com.project.manager.ui.setting.flow_data;

public class TagGroupData {
    private final String group_name;      //标签组名称
    private final long group_no;          //标签组编号

    public String getGroup_name() {
        return group_name;
    }

    public long getGroup_no() {
        return group_no;
    }

    TagGroupData(String group_name, long group_no) {
        this.group_name = group_name;
        this.group_no = group_no;
    }
}
