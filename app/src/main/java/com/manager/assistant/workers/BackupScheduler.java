package com.manager.assistant.workers;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.manager.assistant.enums.LogTags;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BackupScheduler {
    //唯一工作ID，用于识别和取消工作
    public static final String BACKUP_WORK_NAME = "auto_backup_work";

    /**
     * 安排定期备份任务
     *
     * @param context        上下文
     * @param intervalMillis 备份间隔（毫秒）
     */
    public static void schedulePeriodicBackup(Context context, long intervalMillis) {
        //创建约束条件
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true) //电量不低于临界值
                .build();

        //创建周期性工作请求
        PeriodicWorkRequest backupWorkRequest = new PeriodicWorkRequest.Builder(
                BackupWorker.class,
                intervalMillis,
                TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();

        //获取WorkManager实例并安排工作
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueueUniquePeriodicWork(
                BACKUP_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,  //如果工作已存在就更新
                backupWorkRequest);

        //打印日志
        WorkInfo info;
        try {
            info = workManager.getWorkInfosForUniqueWork(BACKUP_WORK_NAME).get().get(0);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        Log.d(LogTags.WORK_STATS.getV(), "State: " + info.getState());
    }

    /**
     * 取消定期备份
     */
    public static void cancelPeriodicBackup(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(BACKUP_WORK_NAME);
    }

    /**
     * 立即执行一次备份
     */
    public static void executeBackupNow(Context context) {
        OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(BackupWorker.class)
                .build();
        WorkManager.getInstance(context).enqueue(oneTimeRequest);
    }
}
