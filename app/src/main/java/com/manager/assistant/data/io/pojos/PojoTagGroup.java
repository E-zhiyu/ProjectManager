package com.manager.assistant.data.io.pojos;

import androidx.annotation.Keep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
@Keep
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
