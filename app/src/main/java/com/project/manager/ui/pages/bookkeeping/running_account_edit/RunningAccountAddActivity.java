package com.project.manager.ui.pages.bookkeeping.running_account_edit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.project.manager.FragmentPagerAdapter;
import com.project.manager.databinding.ActivityRunningAccountAddBinding;
import com.project.manager.ui.pages.bookkeeping.running_account_edit.fragments.ExpenseFragment;
import com.project.manager.ui.pages.bookkeeping.running_account_edit.fragments.IncomeFragment;
import com.project.manager.ui.pages.bookkeeping.running_account_edit.fragments.RunningAccountFragmentBase;
import com.project.manager.ui.pages.bookkeeping.running_account_edit.fragments.TransferFragment;
import com.project.manager.ui.RequestResultCode;

import java.util.ArrayList;
import java.util.List;

public class RunningAccountAddActivity extends AppCompatActivity {
    private RunningAccountFragmentBase current_fragment;    //翻页视图显示的Fragment
    private ActivityRunningAccountAddBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRunningAccountAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    //初始化视图
    private void initViews() {
        //设置标题栏的图标点击监听器
        MaterialToolbar toolbar = binding.toolbar;
        toolbar.setNavigationOnClickListener(v -> finish());

        //为完成按钮绑定单击监听器
        binding.finishBtn.setOnClickListener(v -> {
            String error = current_fragment.verifyInputData();

            //判断是否获取到警告消息（null:无警告，验证通过）
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            } else {
                onFinishBtnClicked();
            }
        });
        binding.cancelBtn.setOnClickListener(v -> finish());

        //创建碎片列表
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new ExpenseFragment());
        fragmentList.add(new IncomeFragment());
        fragmentList.add(new TransferFragment());

        //初始化ViewPager并设置ViewPager适配器
        ViewPager2 viewPager2 = binding.viewPager2;  //翻页视图
        FragmentPagerAdapter viewPagerAdapter = new FragmentPagerAdapter(this, fragmentList);
        viewPager2.setAdapter(viewPagerAdapter);

        //绑定ViewPager和TabLayout
        TabLayout tabLayout = binding.runningAccountAddTabLayout;
        new TabLayoutMediator(
                tabLayout,
                viewPager2,
                (tab, position) -> tab.setText(((RunningAccountFragmentBase) fragmentList.get(position)).getName())
        ).attach();

        //定义翻页回调以刷新活动的fragment实例
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                current_fragment = (RunningAccountFragmentBase) (viewPagerAdapter.getFragment(position));
            }
        });
        viewPager2.setOffscreenPageLimit(1);    //设置保留邻近Fragment
    }

    /**
     * 完成流水新建
     */
    private void onFinishBtnClicked() {
        Intent result2BookKeeping = new Intent();
        Bundle dataBundle = current_fragment.getInputData();    //获取输入的信息并打包

        result2BookKeeping.putExtras(dataBundle);
        setResult(RequestResultCode.RESULT_OK.ordinal(), result2BookKeeping);
        finish();
    }
}