package com.project.manager.ui.pages.bookkeeping.running_account_edit.fragments;

import android.view.View;

import androidx.annotation.NonNull;

import com.project.manager.R;

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
    public void onClick(@NonNull View v) {
        super.onClick(v);
    }

    @Override
    public String verifyInputData() {
        return super.verifyInputData();
    }
}