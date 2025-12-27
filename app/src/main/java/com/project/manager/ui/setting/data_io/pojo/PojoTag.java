package com.project.manager.ui.setting.data_io.pojo;

//标签数据POJO类
public class PojoTag {
    private String name;    //名称
    private long tno;       //编号
    private long group_no;  //标签分组编号

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

    public PojoTag(String name, long tno, long group_no) {
        this.name = name;
        this.tno = tno;
        this.group_no = group_no;
    }

    public PojoTag() {

    }
}
