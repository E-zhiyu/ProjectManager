package com.project.manager.ui.bookkeeping.running_account_edit.fragments;

import android.view.View;

import androidx.annotation.NonNull;

import com.project.manager.R;

public class ExpenseFragment extends RunningAccountFragmentBase {
    public ExpenseFragment() {
        this.name = "支出";                       //为碎片命名
        this.default_remark = "一条支出记录";     //设置默认备注
        this.type = RunningAccountType.EXPENSE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_expense;
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
