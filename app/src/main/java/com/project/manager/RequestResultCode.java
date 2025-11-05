package com.project.manager;

public enum RequestResultCode {
    REQUEST_NEW_FLOW,   //新建流水
    REQUEST_MODIFY_FLOW,//编辑流水
    REQUEST_NEW_TAG,    //新建标签
    REQUEST_MODIFY_TAG, //编辑标签
    REQUEST_CREATE_FILE, //通过SAF向外部存储写入文件
    REQUEST_READ_FILE,  //通过SAF从外部存储读取文件
    RESULT_OK,          //应答接受
    RESULT_REJECT,      //应答拒绝
    RESULT_DELETE       //应答删除
}
