package com.project.manager;

import android.app.Application;

import com.project.manager.ui.view_model.tag_modify.AccountTagViewModel;

public class ManagerAssistant extends Application {
    AccountTagViewModel accountTagViewModel;            //同步标签数据的ViewModel

    @Override
    public void onCreate() {
        super.onCreate();

        //Application初始化时创建ViewModel
        accountTagViewModel = new AccountTagViewModel(this);
    }

    public AccountTagViewModel getAccountTagViewModel() {
        return accountTagViewModel;
    }
}
