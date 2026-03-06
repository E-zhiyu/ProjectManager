package com.manager.assistant.automation.schedulers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import com.manager.assistant.automation.broadcast.BudgetResetReceiver;
import com.manager.assistant.generic_enums.RequestResultCode;

import java.time.LocalDate;
import java.time.ZoneId;

public class BudgetResetScheduler {
    /**
     * 注册预算自动重置检查闹钟，在第二天0点自动检查并重置需要重置的预算
     *
     * @param context 上下文
     */
    public static void scheduleNextMidnight(@NonNull Context context) {
        //获取时区
        ZoneId zone = ZoneId.systemDefault();

        //得到明天的日期
        LocalDate tomorrow = LocalDate.now(zone).plusDays(1);

        //转换为时间戳
        long nextMidnight = tomorrow
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli();

        //获取闹钟管理器
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //设置单一的闹钟
        Intent intent = new Intent(context, BudgetResetReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                RequestResultCode.REQUEST_BUDGET_RESET_ALARM.ordinal(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextMidnight,
                        pi
                );
            }
        } else {
            am.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextMidnight,
                    pi
            );
        }
    }
}
