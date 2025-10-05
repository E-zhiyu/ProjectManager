package com.project.manager.pages.newflow;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.project.manager.pages.newflow.fragments.NewFlowFragmentBase;

import java.util.List;

public class NewFlowFragmentAdapter extends FragmentPagerAdapter {
    private final List<NewFlowFragmentBase> fragmentList;  //碎片列表

    //适配器构造方法
    public NewFlowFragmentAdapter(@NonNull FragmentManager fm, List<NewFlowFragmentBase> fragments) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragmentList = fragments;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentList.get(position).name;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
