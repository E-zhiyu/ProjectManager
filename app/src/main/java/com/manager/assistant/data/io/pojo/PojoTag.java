package com.manager.assistant.data.io.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//标签数据POJO类
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class PojoTag {
    private String name;    //名称
    private long tno;       //编号
    private long group_no;  //标签分组编号
    private int scope;      //作用域

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTno() {
        return tno;
    }

    public void setTno(long tno) {
        this.tno = tno;
    }

    public long getGroup_no() {
        return group_no;
    }

    public void setGroup_no(long group_no) {
        this.group_no = group_no;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public PojoTag(String name, long tno, long group_no, int scope) {
        this.name = name;
        this.tno = tno;
        this.group_no = group_no;
        this.scope = scope;
    }

    public PojoTag() {

    }
}
