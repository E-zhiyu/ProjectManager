package com.manager.assistant.data.classes;

import java.util.ArrayList;
import java.util.List;

public class TagGroup {
    private final List<Tag> tags;   //该分组下的标签字符串
    private String group_name;      //标签组名称
    private final long group_no;    //标签组编号

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public long getGroup_no() {
        return group_no;
    }

    /**
     * 未指定标签的构造方法
     *
     * @param group_name 标签分组名
     * @param group_no   标签分组编号
     */
    public TagGroup(String group_name, long group_no) {
        this.group_name = group_name;
        this.group_no = group_no;
        this.tags = new ArrayList<>();
    }

    /**
     * 添加标签到该分组
     *
     * @param tag 被添加的标签名
     */
    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    /**
     * 删除标签
     *
     * @param index 待删除标签的下标
     */
    public void removeTag(int index) {
        this.tags.remove(index);
    }
}
