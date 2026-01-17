package com.manager.assistant.ui.pages.bookkeeping.running_account.fragments;

import com.manager.assistant.R;

public class IncomeFragment extends RunningAccountFragmentBase {
    public IncomeFragment() {
        this.type = RunningAccountType.INCOME;
    }

    @Override
    protected void setDefaultRemark() {
        this.defaultRemark = "一条收入记录";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_income;
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