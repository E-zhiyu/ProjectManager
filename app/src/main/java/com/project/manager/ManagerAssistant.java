package com.project.manager;

import android.app.Application;
import android.util.Log;

import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.project.manager.data.data_save.preference.AutoBackupPreference;
import com.project.manager.data.data_save.preference.AppSettingsPreference;
import com.project.manager.helpers.AutoBackupHelper;
import com.project.manager.workers.BackupScheduler;

import java.util.concurrent.ExecutionException;

public class ManagerAssistant extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //初始化动态配色
        if (AppSettingsPreference.getDynamicColorStat(this)) {
            DynamicColorsOptions options = new DynamicColorsOptions.Builder()
                    .setThemeOverlay(R.style.Theme_ManagerAssistant_Dynamic)
                    .build();
            DynamicColors.applyToActivitiesIfAvailable(this, options);
        }

        //安排自动备份任务
        if (AutoBackupPreference.getSwitchStat(this)) {
            int frequency_index = AutoBackupPreference.getBackupFrequency(this);
            long intervalMillis = AutoBackupHelper.BackupFrequency.values()[frequency_index].getIntervalMillis();
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
    }
}
