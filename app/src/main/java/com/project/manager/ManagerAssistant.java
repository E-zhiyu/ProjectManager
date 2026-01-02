package com.project.manager;

import android.app.Application;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
import com.project.manager.data.data_save.preference.AutoBackupPreference;
import com.project.manager.data.data_save.preference.AppSettingsPreference;
import com.project.manager.helpers.AutoBackupHelper;
import com.project.manager.ui.view_model.tag_modify.AccountTagViewModel;
import com.project.manager.workers.BackupScheduler;

public class ManagerAssistant extends Application {
    AccountTagViewModel accountTagViewModel;            //同步标签数据的ViewModel

    @Override
    public void onCreate() {
        super.onCreate();

        //Application初始化时创建ViewModel
        accountTagViewModel = new AccountTagViewModel(this);

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
        }
    }

    public AccountTagViewModel getAccountTagViewModel() {
        return accountTagViewModel;
    }
}
