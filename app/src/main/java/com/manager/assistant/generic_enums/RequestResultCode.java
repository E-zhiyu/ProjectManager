package com.manager.assistant.generic_enums;

public enum RequestResultCode {
    RESULT_CANCEL,                      //应答拒绝
    RESULT_OK,                          //应答接受
    RESULT_DELETE,                      //应答删除
    RESULT_MERGE,                       //合并（分组或标签）
    REQUEST_APP_LIST_PERMISSION,        //申请应用列表权限
    REQUEST_NOTIFICATION_PERMISSION,    //申请通知权限
    REQUEST_BUDGET_RESET_ALARM          //设置预算重置闹钟
}
