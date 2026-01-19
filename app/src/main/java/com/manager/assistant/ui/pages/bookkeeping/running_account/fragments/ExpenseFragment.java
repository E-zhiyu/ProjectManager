package com.manager.assistant.ui.pages.bookkeeping.running_account.fragments;

import com.manager.assistant.R;

public class ExpenseFragment extends RunningAccountFragmentBase {
    public ExpenseFragment() {
        this.type = RunningAccountType.EXPENSE;
    }

    @Override
    protected void setDefaultRemark() {
        this.defaultRemark = "一条支出记录";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_expense;
    }

    @Override
    protected void initViews() {
        super.initViews();
    }

    @Override
    public String verifyInputData() {
        return super.verifyInputData();
    }
}
