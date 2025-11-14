package com.project.manager.ui.bookkeeping.running_account_edit.fragments;

import android.view.View;

import androidx.annotation.NonNull;

import com.project.manager.R;

public class IncomeFragment extends RunningAccountFragmentBase {
    public IncomeFragment() {
        this.name = "收入";  //为碎片命名
        this.type = RunningAccountType.INCOME;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_income;
    }

    @Override
    protected void initViews(@NonNull View view) {
        super.initViews(view);
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