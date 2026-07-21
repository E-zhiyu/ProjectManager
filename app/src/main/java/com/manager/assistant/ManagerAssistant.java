package com.manager.assistant;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.manager.assistant.automation.workers.BackupWorker;
import com.manager.assistant.auxiliary.enums.TagStrings;
import com.manager.assistant.data.save.preference.AutoBackupPreference;
import com.manager.assistant.data.save.preference.AppSettingsPreference;
import com.manager.assistant.data.save.preference.VersionPreference;
import com.manager.assistant.auxiliary.enums.LogTags;
import com.manager.assistant.auxiliary.enums.settings.BackupFrequency;
import com.manager.assistant.helpers.NotificationHelper;
import com.manager.assistant.automation.workers.WorkerScheduler;
import com.manager.assistant.helpers.appearence.ThemeHelper;
import com.manager.assistant.helpers.time.AlarmHelper;

import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ManagerAssistant extends Application {
    private static boolean isLifecycleObserverLocked = false;   //生命周期观察者是否被锁定

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
            initThemeMode();

            //注册Activity生命周期监听器
            LifecycleManager.init(this);

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
     * 初始化深浅色主题模式
     */
    private void initThemeMode() {
        int themeMode = AppSettingsPreference.getThemeMode(this);
        ThemeHelper.applyTheme(themeMode);
    }

    /**
     * 锁定生命周期观察者
     */
    public static void lockLifecycleObserver() {
        isLifecycleObserverLocked = true;
    }
}
