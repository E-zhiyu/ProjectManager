package com.project.manager;

import android.app.Application;

import com.google.android.material.color.DynamicColors;
import com.project.manager.data.data_save.preference.ThemePreference;
import com.project.manager.ui.view_model.tag_modify.AccountTagViewModel;

public class ManagerAssistant extends Application {
    AccountTagViewModel accountTagViewModel;            //同步标签数据的ViewModel

    @Override
    public void onCreate() {
        super.onCreate();

        //Application初始化时创建ViewModel
        accountTagViewModel = new AccountTagViewModel(this);

        //初始化动态配色
        if (ThemePreference.getDynamicColorStat(this)) {
            DynamicColors.applyToActivitiesIfAvailable(this);
        }
    }

    public AccountTagViewModel getAccountTagViewModel() {
        return accountTagViewModel;
    }
}
