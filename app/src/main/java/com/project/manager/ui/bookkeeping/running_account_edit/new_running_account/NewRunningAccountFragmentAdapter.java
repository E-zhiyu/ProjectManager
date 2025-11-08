package com.project.manager.ui.bookkeeping.running_account_edit.new_running_account;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountFragmentBase;

import java.util.List;

public class NewRunningAccountFragmentAdapter extends FragmentPagerAdapter {
    private final List<RunningAccountFragmentBase> fragmentList;  //碎片列表

    //适配器构造方法
    public NewRunningAccountFragmentAdapter(@NonNull FragmentManager fm, List<RunningAccountFragmentBase> fragments) {
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
        return fragmentList.get(position).getName();
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
