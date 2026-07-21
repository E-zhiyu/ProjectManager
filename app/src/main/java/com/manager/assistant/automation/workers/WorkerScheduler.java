package com.manager.assistant.automation.workers;

import android.content.Context;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.manager.assistant.auxiliary.enums.LogTags;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class WorkerScheduler {
    /**
     * 安排重复 Worker 任务
     *
     * @param context        上下文
     * @param intervalMillis 备份间隔（毫秒）
     * @param tag            Worker 的标签
     * @param workerClass    Worker 的类型
     */
    public static void schedulePeriodicBackup(Context context, long intervalMillis, String tag, Class<? extends ListenableWorker> workerClass) {
        //创建约束条件
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true) //电量不低于临界值
                .build();

        //创建周期性工作请求
        PeriodicWorkRequest backupWorkRequest = new PeriodicWorkRequest.Builder(
                workerClass,
                intervalMillis,
                TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();

        //获取WorkManager实例并安排工作
        WorkManager workManager = WorkManager.getInstance(context);
        workManager.enqueueUniquePeriodicWork(
                tag,
                ExistingPeriodicWorkPolicy.UPDATE,  //如果工作已存在就更新
                backupWorkRequest);

        //打印日志
        try {
            WorkInfo info = workManager.getWorkInfosForUniqueWork(tag).get().get(0);
            Log.d(LogTags.WORK_STATS.n(), "State: " + info.getState());
        } catch (ExecutionException | InterruptedException e) {
            Log.d(LogTags.WORK_STATS.n(), "State: " + workerClass + "未正常工作");
        }
    }

    /**
     * 取消 Worker 的自动任务
     *
     * @param context   上下文
     * @param workerTag Worker 对应的标签
     */
    public static void cancelPeriodicBackup(Context context, String workerTag) {
        WorkManager.getInstance(context).cancelUniqueWork(workerTag);
    }

    /**
     * 立即执行一次 Worker 中的任务
     *
     * @param context     上下文
     * @param workerClass Worker 的类型
     */
    public static void executeWorkOnceNow(Context context, Class<? extends ListenableWorker> workerClass) {
        OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(workerClass)
                .build();
        WorkManager.getInstance(context).enqueue(oneTimeRequest);
    }
}
