package com.project.manager.pages.newflow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public abstract class NewFlowFragmentBase extends Fragment {
    public String name;  //碎片名称

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResId(), container, false);
        initViews(view);
        return view;
    }

    protected abstract int getLayoutResId();

    //初始化碎片布局
    protected abstract void initViews(View view);
}