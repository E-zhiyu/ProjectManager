package com.project.manager.pages.newflow.fragments;

import android.view.View;

import com.project.manager.R;

public class IncomeFragment extends NewFlowFragmentBase {
    public IncomeFragment() {
        name = "收入";  //为碎片命名
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_income;
    }

    @Override
    protected void initViews(View view) {

    }
}