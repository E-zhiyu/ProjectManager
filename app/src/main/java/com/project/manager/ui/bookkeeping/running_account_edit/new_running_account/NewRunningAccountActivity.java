package com.project.manager.ui.bookkeeping.running_account_edit.new_running_account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.project.manager.R;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.ExpenseFragment;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.IncomeFragment;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountFragmentBase;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.TransferFragment;
import com.project.manager.ResultCode;

import java.util.ArrayList;
import java.util.List;

public class NewRunningAccountActivity extends AppCompatActivity implements View.OnClickListener {
    ViewPager2 runningAccountFragmentPager;         //翻页视图
    RunningAccountFragmentBase current_fragment;    //翻页视图显示的Fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_running_account);

        //创建碎片列表
        List<RunningAccountFragmentBase> fragmentList = new ArrayList<>();
        fragmentList.add(new ExpenseFragment());
        fragmentList.add(new IncomeFragment());
        fragmentList.add(new TransferFragment());

        //初始化ViewPager并设置ViewPager适配器
        runningAccountFragmentPager = findViewById(R.id.new_running_account_pager);
        NewRunningAccountFragmentAdapter viewPagerAdapter = new NewRunningAccountFragmentAdapter(this, fragmentList);
        runningAccountFragmentPager.setAdapter(viewPagerAdapter);

        //绑定ViewPager和TabLayout
        TabLayout tabLayout = findViewById(R.id.new_running_account_tab_layout);
        new TabLayoutMediator(
                tabLayout,
                runningAccountFragmentPager,
                (tab, position) -> tab.setText(fragmentList.get(position).getName())
        ).attach();

        //获取当前Fragment
        runningAccountFragmentPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                current_fragment = viewPagerAdapter.getFragment(position);
            }
        });

        //为完成按钮绑定单击监听器
        findViewById(R.id.finish_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.finish_btn) {
            String error = current_fragment.verifyInputData();

            //判断是否获取到警告消息（null:无警告，验证通过）
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            } else {
                onFinishBtnClicked();
            }
        }
    }

    /**
     * 完成流水新建
     */
    private void onFinishBtnClicked() {
        Intent result2BookKeeping = new Intent();
        Bundle dataBundle = current_fragment.getInputData();    //获取输入的信息并打包

        result2BookKeeping.putExtras(dataBundle);
        setResult(ResultCode.RESULT_OK.ordinal(), result2BookKeeping);
        finish();
    }
}