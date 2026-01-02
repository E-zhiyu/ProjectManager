package com.project.manager.ui.bookkeeping.running_account_edit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.project.manager.R;
import com.project.manager.databinding.ActivityRunningAccountModifyBinding;
import com.project.manager.ui.RequestResultCode;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.ExpenseFragment;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountFragmentBase;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.IncomeFragment;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.TransferFragment;

public class RunningAccountModifyActivity extends AppCompatActivity {
    private RunningAccountType type = null;                         //流水种类
    private int position = -1;                                      //流水项目的下标
    private long rno;                                               //流水编号
    private RunningAccountFragmentBase runningAccountFragment;      //流水账数据输入碎片
    private ActivityRunningAccountModifyBinding binding;            //绑定的XML视图引用

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRunningAccountModifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();

        //接收种类和下标参数
        Bundle dataBundle = getIntent().getExtras();
        if (dataBundle != null) {
            type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
            position = dataBundle.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());
            rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());
        } else {
            NullPointerException e = new NullPointerException("编辑流水时无法读取原有的数据");
            ExceptionHelper.showExceptionDialog(this, e);
        }

        if (savedInstanceState == null) {
            //创建流水编辑Fragment实例（第一次创建界面时）
            if (type == RunningAccountType.EXPENSE) {
                runningAccountFragment = new ExpenseFragment();
            } else if (type == RunningAccountType.INCOME) {
                runningAccountFragment = new IncomeFragment();
            } else if (type == RunningAccountType.TRANSFER) {
                runningAccountFragment = new TransferFragment();
            } else {
                NullPointerException e = new NullPointerException("无法创建有效的流水数据Fragment");
                ExceptionHelper.showExceptionDialog(this, e);
                return;
            }

            //将Fragment添加到布局
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.running_account_edit_fragment_container, runningAccountFragment);
            transaction.commit();
            runningAccountFragment.receiveInitData(dataBundle);   //将原本的数据传递给碎片实例
        }
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

        //为按钮设置单击监听器
        binding.cancelBtn.setOnClickListener(v -> finish());
        binding.finishBtn.setOnClickListener(v -> {
            Intent result2BookKeeping = new Intent();
            String error;
            error = runningAccountFragment.verifyInputData();

            //判断是否获取到报错消息（null:无报错，验证通过）
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            } else {
                Bundle dataBundle = getInputData();
                result2BookKeeping.putExtras(dataBundle);
                setResult(RequestResultCode.RESULT_OK.ordinal(), result2BookKeeping);
                finish();
            }
        });
        binding.deleteBtn.setOnClickListener(v -> {
            Intent result2BookKeeping = new Intent();
            new MaterialAlertDialogBuilder(this)
                    .setTitle("删除流水记录")
                    .setMessage("此流水记录将会被永久删除，确认继续吗？")
                    .setPositiveButton("确认", (dialog, which) -> {
                        Bundle dataBundle = new Bundle();
                        dataBundle.putInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), position);
                        result2BookKeeping.putExtras(dataBundle);
                        setResult(RequestResultCode.RESULT_DELETE.ordinal(), result2BookKeeping);
                        finish();
                    })
                    .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    /**
     * 获取修改后的数据
     *
     * @return 包含修改后数据的包裹
     */
    @NonNull
    private Bundle getInputData() {
        Bundle dataBundle = runningAccountFragment.getInputData();

        dataBundle.putInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), position);       //流水视图下标
        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString());     //流水种类
        dataBundle.putLong(KeyValueStrings.ACCOUNT_NO.getValue(), rno);                     //流水编号

        return dataBundle;
    }
}