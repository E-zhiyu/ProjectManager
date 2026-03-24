package com.manager.assistant.data.classes;

public class Tag {
    private String name;    //名称
    private final long tno; //编号
    private int scope;      //作用域

    public Tag(String name, long tno, int scope) {
        this.name = name;
        this.tno = tno;
        this.scope = scope;
    }

    public Tag(String name, long tno) {
        this.name = name;
        this.tno = tno;
        this.scope = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String new_name) {
        this.name = new_name;
    }

    public long getTno() {
        return tno;
    }

    public int getScope() {
        return scope;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }
}
