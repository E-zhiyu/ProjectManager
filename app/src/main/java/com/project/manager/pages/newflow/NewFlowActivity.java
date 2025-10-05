package com.project.manager.pages.newflow;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.project.manager.R;
import com.project.manager.pages.newflow.fragments.ExpenseFragment;
import com.project.manager.pages.newflow.fragments.IncomeFragment;
import com.project.manager.pages.newflow.fragments.NewFlowFragmentBase;
import com.project.manager.pages.newflow.fragments.TransferFragment;

import java.util.ArrayList;
import java.util.List;

public class NewFlowActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_flow);

        //创建碎片列表
        List<NewFlowFragmentBase> fragmentList = new ArrayList<>();
        fragmentList.add(new ExpenseFragment());
        fragmentList.add(new IncomeFragment());
        fragmentList.add(new TransferFragment());

        //初始化ViewPager并设置ViewPager适配器
        ViewPager viewPager = findViewById(R.id.new_flow_pager);
        NewFlowFragmentAdapter adapter = new NewFlowFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(adapter);

    }
}