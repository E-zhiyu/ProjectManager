package com.project.manager.ui.bookkeeping.running_account_edit.fragments;

public enum RunningAccountType {
    EXPENSE("支出"),
    INCOME("收入"),
    TRANSFER("转账");
    final String title;

    RunningAccountType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
