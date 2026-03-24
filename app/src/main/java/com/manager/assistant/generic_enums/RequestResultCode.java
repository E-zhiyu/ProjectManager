package com.manager.assistant.generic_enums;

public enum RequestResultCode {
    RESULT_CANCEL,                      //应答拒绝
    RESULT_OK,                          //应答接受
    RESULT_DELETE,                      //应答删除
    RESULT_MERGE,                       //合并（分组或标签）
    REQUEST_APP_LIST_PERMISSION,        //申请应用列表权限
    REQUEST_NOTIFICATION_PERMISSION,    //申请通知权限
    REQUEST_BUDGET_RESET_ALARM,         //设置预算重置闹钟
    REQUEST_BUDGET_NOTIFICATION,        //预算余额提醒通知发送
    REQUEST_KEEP,                       //自动记账通知中保留按钮的请求代码
    REQUEST_DELETE,                     //自动记账通知中删除按钮的请求代码
    REQUEST_INPUT_REMARK,               //自动记账通知中输入备注的请求代码
}
