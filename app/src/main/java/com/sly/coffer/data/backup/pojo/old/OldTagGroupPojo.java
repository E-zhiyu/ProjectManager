package com.sly.coffer.data.backup.pojo.old;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class OldTagGroupPojo {
    private String group_name;
    private long group_no;

    public OldTagGroupPojo() {
    }

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
}
