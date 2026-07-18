package com.manager.assistant.automation.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.manager.assistant.data.controllers.BudgetDataController;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.helpers.time.AlarmHelper;

public class BudgetResetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LogTags.BUDGET_RESET_RECEIVER.n(), "预算重置闹钟已触发");
        BudgetDataController.resetAutomaticallyIfNeed(context);
        AlarmHelper.setBudgetCheckAlarm(context);
    }
}
