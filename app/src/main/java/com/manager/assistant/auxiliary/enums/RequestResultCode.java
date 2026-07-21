package com.manager.assistant.auxiliary.enums;

public enum RequestResultCode {
    RESULT_CANCEL,                      //应答拒绝
    RESULT_OK,                          //应答接受
    RESULT_DELETE,                      //应答删除
    RESULT_MERGE,                       //合并（分组或标签）
    REQUEST_BUDGET_RESET_ALARM,         //设置预算重置闹钟
    REQUEST_SKIP_ANALYSIS_RULE_MANAGE,  //跳转至通知解析规则管理
}
