package com.project.manager;

import android.app.Application;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;
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
            DynamicColorsOptions options = new DynamicColorsOptions.Builder()
                    .setThemeOverlay(R.style.Theme_ManagerAssistant_Dynamic)
                    .build();
            DynamicColors.applyToActivitiesIfAvailable(this, options);
        }
    }

    public AccountTagViewModel getAccountTagViewModel() {
        return accountTagViewModel;
    }
}
