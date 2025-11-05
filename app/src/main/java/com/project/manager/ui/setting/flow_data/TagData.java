package com.project.manager.ui.setting.flow_data;

//标签数据POJO类
public class TagData {
    private final String name;    //名称
    private final long tno;       //编号
    private final long group_no;  //标签分组编号

    public String getName() {
        return name;
    }

    public long getTno() {
        return tno;
    }

    public long getGroup_no() {
        return group_no;
    }

    TagData(String name, long tno, long group_no) {
        this.name = name;
        this.tno = tno;
        this.group_no = group_no;
    }
}
