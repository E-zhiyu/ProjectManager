package com.project.manager;

import android.app.Application;

import com.project.manager.ui.bookkeeping.running_account_edit.fragments.view_model.AccountTagViewModel;

public class ProjectManager extends Application {
    AccountTagViewModel accountTagViewModel;    //共享标签数据的ViewModel

    @Override
    public void onCreate() {
        super.onCreate();

        // Application初始化时创建ViewModel
        accountTagViewModel = new AccountTagViewModel(this);
    }

    public AccountTagViewModel getAccountTagViewModel() {
        return accountTagViewModel;
    }
}
