package com.manager.assistant.ui.data_sync.tag_modify;

/**
 * 标签更新原因
 */
public enum TagUpdateReason {
    REFRESH,    //刷新（如导入数据时）
    CLEAR,      //清空
    ADD,        //添加标签
    RENAME,     //修改名称
    DELETE,     //删除
    MERGE       //合并
}
