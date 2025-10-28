package com.project.manager.ui.bookkeeping.tag;

import java.util.ArrayList;
import java.util.List;

public class TagGroup {
    List<String> tags;  //该分组下的标签字符串
    String group_name;  //标签组名称

    /**
     * 指定标签的构造方法
     *
     * @param group_name 标签分组名
     * @param tags       该分组下的标签名列表
     */
    public TagGroup(String group_name, List<String> tags) {
        this.group_name = group_name;
        this.tags = tags;
    }

    /**
     * 未指定标签的构造方法
     *
     * @param group_name 标签分组名
     */
    public TagGroup(String group_name) {
        this.group_name = group_name;
        this.tags = new ArrayList<>();
    }

    /**
     * 添加标签到该分组
     *
     * @param tag 被添加的标签名
     */
    public void addTag(String tag) {
        this.tags.add(tag);
    }
}
