package com.manager.assistant;

import android.app.Application;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.manager.assistant.data.data_save.preference.AutoBackupPreference;
import com.manager.assistant.data.data_save.preference.AppSettingsPreference;
import com.manager.assistant.data.data_save.preference.VersionPreference;
import com.manager.assistant.isolated_enums.LogTags;
import com.manager.assistant.helpers.AutoBackupHelper;
import com.manager.assistant.workers.BackupScheduler;

import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ManagerAssistant extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && getProcessName().equals(getPackageName())) {
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

            //启动时检测是否有需要删除的按转包
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
}
