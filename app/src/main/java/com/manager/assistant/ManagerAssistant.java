package com.manager.assistant;

import android.app.Activity;
import android.app.Application;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.manager.assistant.automation.schedulers.BudgetResetScheduler;
import com.manager.assistant.data.save.preference.AutoBackupPreference;
import com.manager.assistant.data.save.preference.AppSettingsPreference;
import com.manager.assistant.data.save.preference.AutoBookKeepingPreference;
import com.manager.assistant.data.save.preference.VersionPreference;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.helpers.NotificationHelper;
import com.manager.assistant.helpers.file.AutoBackupHelper;
import com.manager.assistant.automation.schedulers.BackupScheduler;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ManagerAssistant extends Application {
    private int foregroundActivities = 0;               // 前台 Activity 数量
    private boolean doNotHideOnce = true;              // 临时不隐藏后台一次（后台隐藏豁免）
    private WeakReference<Activity> rootActivityRef;    // 任务根 Activity 引用

    @Override
    public void onCreate() {
        super.onCreate();

        //注册预算重置检查闹钟
        BudgetResetScheduler.scheduleNextMidnight(this);

        //注册通知渠道
        NotificationHelper.createNotificationChannels(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && getProcessName().equals(getPackageName())) {
            //初始化动态配色
            if (AppSettingsPreference.getDynamicColorStat(this)) {
                DynamicColorsOptions options = new DynamicColorsOptions.Builder()
                        .setThemeOverlay(R.style.Theme_ManagerAssistant_Dynamic)
                        .build();
                DynamicColors.applyToActivitiesIfAvailable(this, options);
            }

            //注册Activity生命周期监听器
            registerLifecycleCallbacks();

            //安排自动备份任务
            if (AutoBackupPreference.getSwitchStat(this)) {
                int frequency = AutoBackupPreference.getBackupFrequency(this);
                long intervalMillis = AutoBackupHelper.BackupFrequency.values()[frequency].getIntervalMillis();
                BackupScheduler.schedulePeriodicBackup(this, intervalMillis);

                //打印任务状态日志
                WorkInfo info;
                try {
                    info = WorkManager.getInstance(this).
                            getWorkInfosForUniqueWork(BackupScheduler.BACKUP_WORK_NAME).
                            get().get(0);
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Log.d(LogTags.WORK_STATS.getV(), "State: " + info.getState());
            }

            //启动时检测是否有需要删除的安装包
            String apkUri = VersionPreference.getApkUri(this);
            if (!apkUri.isEmpty()) {
                File apkFile = new File(Objects.requireNonNull(Uri.parse(apkUri).getPath()));
                if (apkFile.exists() && apkFile.delete()) {
                    Log.d(LogTags.APPLICATION.getV(), String.format(Locale.getDefault(), "成功删除“%s”", apkFile.getName()));
                    VersionPreference.setApkUri(this, "");
                } else {
                    Log.w(LogTags.APPLICATION.getV(), String.format(Locale.getDefault(), "“%s”删除失败", apkFile.getName()));
                }
            }
        }
    }

    /**
     * 注册Activity生命周期监听器
     * 核心思想：当前台Activity计数器为0时，说明应用在后台，根据用户配置决定是否移除根Activity的最近任务卡片
     */
    private void registerLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                if (activity.isTaskRoot()) {
                    // 保存任务根 Activity
                    rootActivityRef = new WeakReference<>(activity);
                }
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                //前台活动计数器+1
                foregroundActivities++;
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                //前台活动计数器-1
                foregroundActivities--;

                //判断前台是否还有活动
                if (foregroundActivities == 0) {
                    Activity rootActivity = rootActivityRef != null ? rootActivityRef.get() : null;
                    boolean isHidden = AutoBookKeepingPreference.getHideRecentTask(ManagerAssistant.this);

                    //判断是否启用了隐藏后台以及是否保存了根Activity引用
                    if (rootActivity != null && isHidden) {
                        //只有需要隐藏后台时才判断是否豁免
                        if (doNotHideOnce) {
                            doNotHideOnce = false;
                            return;
                        }

                        //移除整个任务，隐藏最近任务
                        rootActivity.finishAndRemoveTask();
                        rootActivityRef = null; //避免重复引用
                    } else {
                        doNotHideOnce = false;  //豁免标识归位
                    }
                }
            }
        });
    }

    /**
     * 豁免一次隐藏最近任务
     */
    public void exemptionHideRecentOnce() {
        doNotHideOnce = true;
    }
}
