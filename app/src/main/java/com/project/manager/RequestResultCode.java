package com.project.manager;

public enum RequestResultCode {
    NEW_FLOW_REQUEST,   //新建流水
    MODIFY_FLOW_REQUEST,//编辑流水
    NEW_TAG_REQUEST,    //新建标签
    MODIFY_TAG_REQUEST, //编辑标签
    RESULT_OK,          //应答接受
    RESULT_REJECT,      //应答拒绝
    RESULT_DELETE       //应答删除
}
