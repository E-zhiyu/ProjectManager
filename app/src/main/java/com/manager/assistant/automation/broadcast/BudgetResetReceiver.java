package com.manager.assistant.automation.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.manager.assistant.automation.schedulers.BudgetResetScheduler;
import com.manager.assistant.data.data_class.Budget;
import com.manager.assistant.generic_enums.LogTags;

public class BudgetResetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LogTags.BUDGET_RESET_RECEIVER.getV(), "预算重置闹钟已触发");
        Budget.resetAutomaticallyIfNeed(context);
        BudgetResetScheduler.scheduleNextMidnight(context);
    }
}
