package com.project.manager.pages.newflow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.project.manager.databinding.FragmentBookkeepingBinding;

public abstract class NewFlowFragmentBase extends Fragment {
    FragmentBookkeepingBinding binding;     //父界面索引
    View xmlView;                           //绑定的XML界面
    public static String name;              //碎片名称
    public static FlowTypeEnum type;        //流水类型

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookkeepingBinding.inflate(inflater, container, false);

        xmlView = inflater.inflate(getLayoutResId(), container, false);
        initViews(xmlView);
        return xmlView;
    }

    protected abstract int getLayoutResId();

    //初始化碎片布局
    protected abstract void initViews(View view);
}

