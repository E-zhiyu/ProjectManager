package com.project.manager.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.project.manager.LogTags;

public class AnalysisRuleUpdateReceiver extends BroadcastReceiver {
    private final OnRuleUpdatedListener listener;

    public interface OnRuleUpdatedListener {
        //规则更新回调
        void onRuleUpdated();
    }

    public AnalysisRuleUpdateReceiver(OnRuleUpdatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        if (BroadcastConstants.ACTION_RULES_UPDATED.toString().equals(intent.getAction())) {
            Log.d(LogTags.RULE_UPDATE_RECEIVER.getV(), "接收广播：规则更新");

            if (listener != null) {
                listener.onRuleUpdated();
            }
        }
    }
}
