package com.project.manager.pages.newflow.fragments;

import android.view.View;

import com.project.manager.R;

public class TransferFragment extends NewFlowFragmentBase {
    public TransferFragment() {
        name = "转账";  //为碎片命名
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_transfer;
    }

    @Override
    protected void initViews(View view) {

    }
}
