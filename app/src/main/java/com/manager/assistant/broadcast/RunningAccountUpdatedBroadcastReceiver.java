package com.manager.assistant.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

public class RunningAccountUpdatedBroadcastReceiver extends BroadcastReceiver {
    private final OnAccountUpdatedListener listener;

    public interface OnAccountUpdatedListener {
        /**
         * 流水账记录增加回调
         *
         * @param dataBundle 新增流水记录的数据
         */
        void onAccountAdded(Bundle dataBundle);
    }

    public RunningAccountUpdatedBroadcastReceiver(OnAccountUpdatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, @NonNull Intent intent) {
        String action = intent.getAction();
        if (BroadcastConstants.ACTION_RUNNING_ACCOUNT_UPDATED.toString().equals(action)) {
            if (listener != null) {
                listener.onAccountAdded(intent.getExtras());
            }
        }
    }
}
