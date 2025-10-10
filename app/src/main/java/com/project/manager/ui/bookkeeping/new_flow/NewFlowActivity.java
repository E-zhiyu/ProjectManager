package com.project.manager.ui.bookkeeping.new_flow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.project.manager.R;
import com.project.manager.ui.bookkeeping.flow_type.ExpenseFragment;
import com.project.manager.ui.bookkeeping.flow_type.FlowTypeEnum;
import com.project.manager.ui.bookkeeping.flow_type.IncomeFragment;
import com.project.manager.ui.bookkeeping.flow_type.FlowFragmentBase;
import com.project.manager.ui.bookkeeping.flow_type.TransferFragment;
import com.project.manager.RequestResultCode;

import java.util.ArrayList;
import java.util.List;

public class NewFlowActivity extends AppCompatActivity implements View.OnClickListener {
    ViewPager viewPager;  //翻页视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_flow);

        //创建碎片列表
        List<FlowFragmentBase> fragmentList = new ArrayList<>();
        fragmentList.add(new ExpenseFragment());
        fragmentList.add(new IncomeFragment());
        fragmentList.add(new TransferFragment());

        //初始化ViewPager并设置ViewPager适配器
        viewPager = findViewById(R.id.new_flow_pager);
        NewFlowFragmentAdapter viewPagerAdapter = new NewFlowFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(viewPagerAdapter);

        //绑定ViewPager和TabLayout
        TabLayout tabLayout = findViewById(R.id.new_flow_tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        //为完成按钮绑定单击监听器
        findViewById(R.id.new_flow_finish_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.new_flow_finish_btn) {
            finishCreatingFlow();
        }
    }

    /**
     * 获取当前翻页视图展示的碎片
     *
     * @param viewPager 翻页视图
     * @return 当前展示的Fragment
     */
    private Fragment getCurrentFragment(ViewPager viewPager) {
        int currentItem = viewPager.getCurrentItem();
        String tag = "android:switcher:" + viewPager.getId() + ":" + currentItem;

        return getSupportFragmentManager().findFragmentByTag(tag);
    }

    /**
     * 完成流水新建
     */
    private void finishCreatingFlow() {
        Intent resultIntent = new Intent();

        //获取当前碎片基本信息并打包
        FlowFragmentBase currentFragment = (FlowFragmentBase) getCurrentFragment(viewPager);
        FlowTypeEnum flowType = currentFragment.getType();  //种类
        resultIntent.putExtra("type", flowType.toString());
        String flowName = currentFragment.getName();        //名称
        resultIntent.putExtra("name", flowName);

        //获取碎片通用信息并打包
        int flowAmount = currentFragment.getAmount();   //金额
        resultIntent.putExtra("amount", String.valueOf(flowAmount));
        String flowDate = currentFragment.getDate();    //日期
        resultIntent.putExtra("date", flowDate);
        String flowRemark = currentFragment.getRemark();//备注
        resultIntent.putExtra("remark", flowRemark);

        //获取碎片特殊信息并打包
        if (flowType == FlowTypeEnum.TRANSFER) {
            TransferFragment transferFragment = (TransferFragment) currentFragment;
            String exportAccount, importAccount;
            exportAccount = transferFragment.getExportAccount();    //转出账户
            resultIntent.putExtra("exportAccount", exportAccount);
            importAccount = transferFragment.getImportAccount();    //转入账户
            resultIntent.putExtra("importAccount", importAccount);
        }

        setResult(RequestResultCode.RESULT_OK.ordinal(), resultIntent);
        finish();
    }
}