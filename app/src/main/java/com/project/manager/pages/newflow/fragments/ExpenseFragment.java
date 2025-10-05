package com.project.manager.pages.newflow.fragments;

import android.view.View;

import com.project.manager.R;

public class ExpenseFragment extends NewFlowFragmentBase {

    public ExpenseFragment() {
        name = "支出";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_expense;
    }

    @Override
    protected void initViews(View view) {

    }
}
