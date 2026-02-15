package com.manager.assistant.ui.data_communication.tag_modify;

/**
 * 标签更新原因
 */
public enum TagUpdateReason {
    ADD,    //完成添加标签回调
    RENAME, //修改名称
    DELETE, //删除
    MERGE   //合并
}
