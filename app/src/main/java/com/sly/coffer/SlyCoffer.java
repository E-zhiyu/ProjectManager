package com.sly.coffer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.sly.coffer.automation.workers.BackupWorker;
import com.sly.coffer.auxiliary.enums.TagStrings;
import com.sly.coffer.auxiliary.enums.settings.AuthOpportunity;
import com.sly.coffer.data.save.preference.AutoBackupPreference;
import com.sly.coffer.data.save.preference.AppSettingsPreference;
import com.sly.coffer.data.save.preference.SecurityPreference;
import com.sly.coffer.data.save.preference.VersionPreference;
import com.sly.coffer.auxiliary.enums.LogTags;
import com.sly.coffer.auxiliary.enums.settings.BackupFrequency;
import com.sly.coffer.helpers.NotificationHelper;
import com.sly.coffer.automation.workers.WorkerScheduler;
import com.sly.coffer.helpers.appearence.ThemeHelper;
import com.sly.coffer.helpers.time.AlarmHelper;
import com.sly.coffer.ui.pages.AuthActivity;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class SlyCoffer extends Application {
    private static boolean isLifecycleObserverLocked = false;   //生命周期观察者是否被锁定
    private int startedActivityCount = 0;                       //在前台的活动数量

    @Override
    public void onCreate() {
        super.onCreate();

        //注册预算重置检查闹钟
        AlarmHelper.setBudgetCheckAlarm(this);

        //注册通知渠道
        NotificationHelper.createNotificationChannels(this);

        if (getProcessName().equals(getPackageName())) {
            //初始化动态配色
            if (AppSettingsPreference.getDynamicColorStat(this)) {
                DynamicColorsOptions options = new DynamicColorsOptions.Builder()
                        .setThemeOverlay(R.style.Theme_ManagerAssistant_Dynamic)
                        .build();
                DynamicColors.applyToActivitiesIfAvailable(this, options);
            }

            //初始化主题模式
            int themeMode = AppSettingsPreference.getThemeMode(this);
            ThemeHelper.applyTheme(themeMode);

            //安排自动备份任务
            if (AutoBackupPreference.getSwitchStat(this)) {
                int frequency = AutoBackupPreference.getBackupFrequency(this);
                long intervalMillis = BackupFrequency.values()[frequency].getIntervalMillis();
                WorkerScheduler.schedulePeriodicBackup(this, intervalMillis, TagStrings.BACKUP_WORKER.t(), BackupWorker.class);

                //打印任务状态日志
                try {
                    WorkInfo info = WorkManager.getInstance(this).getWorkInfosForUniqueWork(TagStrings.BACKUP_WORKER.t()).get().get(0);
                    Log.d(LogTags.WORK_STATS.n(), "State: " + info.getState());
                } catch (ExecutionException | InterruptedException e) {
                    Log.d(LogTags.WORK_STATS.n(), "State: " + BackupWorker.class + "未正常工作");
                }
            }

            //注册应用级的生命周期观察者
            ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onStart(@NonNull LifecycleOwner owner) {
                    Log.d(LogTags.APPLICATION.n(), "触发全局生命周期观察者的onStart()");
                    if (isLifecycleObserverLocked) {
                        Log.d(LogTags.APPLICATION.n(), "消费掉生命周期观察者的锁");
                        isLifecycleObserverLocked = false;  //重新启动时消费掉锁定
                        return;
                    }

                    // 应用进入前台，检查是否需要解锁
                    long currentTimeMillis = System.currentTimeMillis();
                    long lastSuccessTimeMillis = AuthActivity.getLastSuccessTimeMillis();
                    long minDifference = AuthOpportunity.values()[
                            SecurityPreference.getAuthOpportunity(SlyCoffer.this)
                            ].getTimeMilli();
                    if (
                            SecurityPreference.getAuthSwitchStat(SlyCoffer.this) &&
                                    currentTimeMillis - lastSuccessTimeMillis >= minDifference
                    ) {
                        Intent intent = new Intent(SlyCoffer.this, AuthActivity.class);
                        // FLAG_ACTIVITY_NEW_TASK 是从 Application 启动 Activity 必须带的
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
            });

            //注册活动生命周期监听器，用于更新活动数量
            registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
                @Override
                public void onActivityStarted(@NonNull Activity activity) {
                    startedActivityCount++;
                }

                @Override
                public void onActivityStopped(@NonNull Activity activity) {
                    startedActivityCount--;

                    if (isLifecycleObserverLocked) return;  //如果生命周期观察者被锁定，不执行任何操作

                    // 当计数器归零，说明【此时此刻】没有任何 Activity 在前台了，用户刚按了 Home 键或锁屏
                    if (startedActivityCount == 0) {
                        if (SecurityPreference.getHideRecentTask(SlyCoffer.this)) {
                            // 这里的 onStop 是跟随 Activity 的，会立刻执行，不会拖延到下次启动
                            removeTaskFromRecents();
                        }
                    }
                }

                // 其他生命周期方法留空...
                @Override
                public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                }

                @Override
                public void onActivityResumed(@NonNull Activity activity) {
                }

                @Override
                public void onActivityPaused(@NonNull Activity activity) {
                }

                @Override
                public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
                }

                @Override
                public void onActivityDestroyed(@NonNull Activity activity) {
                }
            });

            //启动时检测是否有需要删除的安装包
            String apkUri = VersionPreference.getApkUri(this);
            if (!apkUri.isEmpty()) {
                File apkFile = new File(Objects.requireNonNull(Uri.parse(apkUri).getPath()));
                if (apkFile.exists() && apkFile.delete()) {
                    Log.d(LogTags.APPLICATION.n(), String.format(Locale.getDefault(), "成功删除“%s”", apkFile.getName()));
                    VersionPreference.setApkUri(this, "");
                } else {
                    Log.w(LogTags.APPLICATION.n(), String.format(Locale.getDefault(), "“%s”删除失败", apkFile.getName()));
                }
            }
        }
    }

    /**
     * 从最近任务中隐藏
     */
    private void removeTaskFromRecents() {
        Log.d(LogTags.APPLICATION.n(), "触发最近任务隐藏");
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            List<ActivityManager.AppTask> taskList = am.getAppTasks();
            if (taskList != null) {
                for (ActivityManager.AppTask task : taskList) {
                    // 立刻移除
                    task.finishAndRemoveTask();
                }
            }
        }
    }

    /**
     * 锁定生命周期观察者
     */
    public static void lockLifecycleObserver() {
        isLifecycleObserverLocked = true;
    }
}
