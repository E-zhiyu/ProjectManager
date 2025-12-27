package com.project.manager.ui.setting.data_io.pojo;

public class PojoTagGroup {
    private String group_name;      //标签组名称
    private long group_no;          //标签组编号

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public long getGroup_no() {
        return group_no;
    }

    public void setGroup_no(long group_no) {
        this.group_no = group_no;
    }

    public PojoTagGroup(String group_name, long group_no) {
        this.group_name = group_name;
        this.group_no = group_no;
    }

    public PojoTagGroup() {

    }
}
