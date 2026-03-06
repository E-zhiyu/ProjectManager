package com.manager.assistant.automation.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.manager.assistant.generic_enums.LogTags;

/**
 * 自动记账相关的广播接收器
 */
public class RuleUpdateReceiver extends BroadcastReceiver {
    private final BroadcastListener listener;

    public interface BroadcastListener {
        //规则更新回调
        void onRuleUpdated();

        //功能开关状态变化回调
        void onFunctionSwitched();
    }

    /**
     * 通知解析相关的广播接收器
     *
     * @param listener 接收广播后执行动作的监听器
     */
    public RuleUpdateReceiver(BroadcastListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        String action = intent.getAction();
        if (BroadcastConstants.ACTION_RULES_UPDATED.toString().equals(action)) {
            Log.d(LogTags.RULE_UPDATE_RECEIVER.getV(), "接收广播：规则更新");

            if (listener != null) {
                listener.onRuleUpdated();
            }
        } else if (BroadcastConstants.ACTION_NOTIFICATION_ANALYSIS_FUNCTION_SWITCHED.toString().equals(action)) {
            Log.d(LogTags.RULE_UPDATE_RECEIVER.getV(), "接收广播：通知解析开关状态变化");

            if (listener != null) {
                listener.onFunctionSwitched();
            }
        }
    }
}
