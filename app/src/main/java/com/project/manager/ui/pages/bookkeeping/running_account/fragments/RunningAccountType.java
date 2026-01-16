package com.project.manager.ui.pages.bookkeeping.running_account.fragments;

public enum RunningAccountType {
    EXPENSE("支出", true),
    INCOME("收入", false),
    TRANSFER("转账", true);
    final String title;
    final boolean isExpenseType;

    RunningAccountType(String title, boolean isExpenseType) {
        this.title = title;
        this.isExpenseType = isExpenseType;
    }

    public String getTitle() {
        return title;
    }

    public boolean isExpenseType() {
        return isExpenseType;
    }
}
