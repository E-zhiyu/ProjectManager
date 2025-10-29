package com.project.manager.ui.bookkeeping.flow_modify.new_flow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.project.manager.R;
import com.project.manager.ui.bookkeeping.FlowAttributeStrings;
import com.project.manager.ui.bookkeeping.flow_modify.fragments.ExpenseFragment;
import com.project.manager.ui.bookkeeping.flow_modify.fragments.FlowTypeEnum;
import com.project.manager.ui.bookkeeping.flow_modify.fragments.IncomeFragment;
import com.project.manager.ui.bookkeeping.flow_modify.fragments.FlowFragmentBase;
import com.project.manager.ui.bookkeeping.flow_modify.fragments.TransferFragment;
import com.project.manager.RequestResultCode;
import com.project.manager.ui.bookkeeping.tag.Tag;

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
            FlowFragmentBase current_fragment = (FlowFragmentBase) getCurrentFragment(viewPager);
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
    //TODO: 将回调函数重命名为on……ed
    private void onFinishBtnClicked() {
        Intent result2BookKeeping = new Intent();
        Bundle dataBundle = new Bundle();

        //获取当前碎片基本信息并打包
        FlowFragmentBase currentFragment = (FlowFragmentBase) getCurrentFragment(viewPager);
        FlowTypeEnum flowType = currentFragment.getType();  //种类
        dataBundle.putString(FlowAttributeStrings.TYPE, flowType.toString());

        //获取碎片通用信息并打包
        double flowAmount = currentFragment.getAmount();    //金额
        dataBundle.putDouble(FlowAttributeStrings.AMOUNT, flowAmount);
        String flowDate = currentFragment.getDate();        //日期
        dataBundle.putString(FlowAttributeStrings.DATETIME, flowDate);
        String flowRemark = currentFragment.getRemark();    //备注
        dataBundle.putString(FlowAttributeStrings.REMARK, flowRemark);
        String tag_name = currentFragment.getFlowTag();     //标签
        int tag_no = Tag.nameTransToTno(tag_name, this);
        dataBundle.putInt(FlowAttributeStrings.TAG_NO, tag_no);

        //获取碎片特殊信息并打包
        if (flowType == FlowTypeEnum.TRANSFER) {
            TransferFragment transferFragment = (TransferFragment) currentFragment;
            String exportAccount, importAccount;
            exportAccount = transferFragment.getExportAccount();    //转出账户
            dataBundle.putString(FlowAttributeStrings.EXPORT, exportAccount);
            importAccount = transferFragment.getImportAccount();    //转入账户
            dataBundle.putString(FlowAttributeStrings.IMPORT, importAccount);
        }

        result2BookKeeping.putExtras(dataBundle);
        setResult(RequestResultCode.RESULT_OK.ordinal(), result2BookKeeping);
        finish();
    }
}