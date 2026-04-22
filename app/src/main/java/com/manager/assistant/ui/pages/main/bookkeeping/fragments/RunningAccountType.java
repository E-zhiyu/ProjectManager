package com.manager.assistant.ui.pages.main.bookkeeping.fragments;

public enum RunningAccountType {
    EXPENSE("支出", -1),
    INCOME("收入", 1),
    TRANSFER("转账", 0);
    private final String title; //名称
    private final int flag;     //是否为收入/支出种类的标识符

    RunningAccountType(String title, int flag) {
        this.title = title;
        this.flag = flag;
    }

    public String getTitle() {
        return title;
    }

    public boolean isExpenseType() {
        return flag == -1;
    }

    public boolean isIncomeType() {
        return flag == 1;
    }
}
