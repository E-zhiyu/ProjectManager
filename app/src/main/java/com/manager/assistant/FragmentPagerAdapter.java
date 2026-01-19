package com.manager.assistant;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class FragmentPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragmentList;  //碎片列表

    //适配器构造方法
    public FragmentPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Fragment> fragmentList) {
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
    public Fragment getFragment(int position) {
        return fragmentList.get(position);
    }
}
