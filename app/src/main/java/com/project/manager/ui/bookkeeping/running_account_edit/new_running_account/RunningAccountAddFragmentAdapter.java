package com.project.manager.ui.bookkeeping.running_account_edit.new_running_account;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountFragmentBase;

import java.util.List;

public class RunningAccountAddFragmentAdapter extends FragmentStateAdapter {
    private final List<RunningAccountFragmentBase> fragmentList;  //碎片列表

    //适配器构造方法
    public RunningAccountAddFragmentAdapter(@NonNull FragmentActivity fragmentActivity, List<RunningAccountFragmentBase> fragmentList) {
        super(fragmentActivity);
        this.fragmentList = fragmentList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    /**
     * 获取指定下标的Fragment
     *
     * @param position 需要获取的Fragment的下标
     * @return 对应下标的Fragment
     */
    public RunningAccountFragmentBase getFragment(int position) {
        return fragmentList.get(position);
    }
}
