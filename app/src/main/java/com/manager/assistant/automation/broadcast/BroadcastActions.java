package com.manager.assistant.automation.broadcast;

/**
 * 广播标识
 */
public enum BroadcastActions {
    ACTION_RULES_UPDATED,                           //通知解析规则更新
    ACTION_NOTIFICATION_ANALYSIS_FUNCTION_SWITCHED, //通知解析功能开关状态变化
    ACTION_RUNNING_ACCOUNT_UPDATED,                 //流水账记录更新
    ACTION_NOTIFICATION_LISTENER_ENABLED,           //通知监听服务成功运行
    ACTION_INPUT_REMARK,                            //自动记账通知中输入备注
    ACTION_KEEP,                                    //保留自动记账产生的流水记录
    ACTION_DELETE,                                  //删除自动记账产生的流水记录
    ACTION_AUTO_BOOKKEEPING_NOTIFICATION_DELETED,   //自动记账确认通知被删除
    ACTION_AUTO_BOOKKEEPING_NOTIFICATION_CLICKED,   //自动记账确认通知被点击
}
