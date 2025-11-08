package com.project.manager.ui.bookkeeping.running_account_edit.new_running_account;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.project.manager.R;
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.ExpenseFragment;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountTypeEnum;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.IncomeFragment;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountFragmentBase;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.TransferFragment;
import com.project.manager.ResultCode;
import com.project.manager.ui.bookkeeping.tag.Tag;

import java.util.ArrayList;
import java.util.List;

public class NewRunningAccountActivity extends AppCompatActivity implements View.OnClickListener {
    ViewPager viewPager;  //翻页视图

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
        viewPager = findViewById(R.id.new_running_account_pager);
        NewRunningAccountFragmentAdapter viewPagerAdapter = new NewRunningAccountFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(viewPagerAdapter);

        //绑定ViewPager和TabLayout
        TabLayout tabLayout = findViewById(R.id.new_running_account_tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        //为完成按钮绑定单击监听器
        findViewById(R.id.finish_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.finish_btn) {
            RunningAccountFragmentBase current_fragment = (RunningAccountFragmentBase) getCurrentFragment(viewPager);
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
    private void onFinishBtnClicked() {
        Intent result2BookKeeping = new Intent();
        Bundle dataBundle = new Bundle();

        //获取当前碎片基本信息并打包
        RunningAccountFragmentBase currentFragment = (RunningAccountFragmentBase) getCurrentFragment(viewPager);
        RunningAccountTypeEnum RunningAccountType = currentFragment.getType();  //种类
        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), RunningAccountType.toString());

        //获取碎片通用信息并打包
        double amount = currentFragment.getAmount();                //金额
        dataBundle.putDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), amount);
        String date_time = currentFragment.getDateTime();           //日期和时间
        dataBundle.putString(KeyValueStrings.ACCOUNT_DATETIME.getValue(), date_time);
        String remark = currentFragment.getRemark();                //备注
        dataBundle.putString(KeyValueStrings.ACCOUNT_REMARK.getValue(), remark);
        String tag_name = currentFragment.getRunningAccountTag();   //标签名称

        //将标签名称转换为标签编号
        try {
            long tag_no = Tag.nameTransToTno(tag_name, this);
            dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(this, e);
        }

        //获取碎片特殊信息并打包
        if (RunningAccountType == RunningAccountTypeEnum.TRANSFER) {
            TransferFragment transferFragment = (TransferFragment) currentFragment;
            String exportAccount, importAccount;
            exportAccount = transferFragment.getExportAccount();    //转出账户
            dataBundle.putString(KeyValueStrings.ACCOUNT_EXPORT.getValue(), exportAccount);
            importAccount = transferFragment.getImportAccount();    //转入账户
            dataBundle.putString(KeyValueStrings.ACCOUNT_IMPORT.getValue(), importAccount);
        }

        result2BookKeeping.putExtras(dataBundle);
        setResult(ResultCode.RESULT_OK.ordinal(), result2BookKeeping);
        finish();
    }
}