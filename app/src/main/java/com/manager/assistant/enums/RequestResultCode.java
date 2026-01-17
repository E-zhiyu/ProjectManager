package com.manager.assistant.enums;

public enum RequestResultCode {
    RESULT_CANCEL,          //应答拒绝
    RESULT_OK,              //应答接受
    RESULT_DELETE,          //应答删除
    RESULT_MERGE,           //合并（分组或标签）
    REQUEST_GET_PERMISSION  //申请权限的请求代码
}
