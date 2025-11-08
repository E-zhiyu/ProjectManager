package com.project.manager.ui.bookkeeping.running_account_edit.fragments;

import android.view.View;

import com.project.manager.R;

public class ExpenseFragment extends RunningAccountFragmentBase {
    public ExpenseFragment() {
        this.name = "支出";  //为碎片命名
        this.type = RunningAccountTypeEnum.EXPENSE;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_expense;
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    public String verifyInputData() {
        return super.verifyInputData();
    }
}
