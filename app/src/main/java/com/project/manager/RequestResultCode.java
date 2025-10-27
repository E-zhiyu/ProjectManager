package com.project.manager;

public enum RequestResultCode {
    NEW_FLOW_REQUEST,   //新建流水
    EDIT_FLOW_REQUEST,  //编辑流水
    NEW_TAG_REQUEST,    //新建标签
    RESULT_OK,          //应答接受
    RESULT_REJECT,      //应答拒绝
    RESULT_DELETE_FLOW  //特殊应答代码：删除指定位置的流水记录
}
