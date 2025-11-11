package com.project.manager.ui.bookkeeping.running_account_edit.modify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.project.manager.R;
import com.project.manager.ResultCode;
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.ExpenseFragment;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountFragmentBase;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountTypeEnum;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.IncomeFragment;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.TransferFragment;

public class RunningAccountModifyActivity extends AppCompatActivity implements View.OnClickListener {
    RunningAccountTypeEnum type = null;                     //流水种类
    int position = -1;                                      //流水项目的下标
    RunningAccountFragmentBase runningAccountFragment;      //流水账数据输入碎片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_account_modify);

        initViews();

        //接收种类和下标参数
        Bundle dataBundle = getIntent().getExtras();
        if (dataBundle != null) {
            type = RunningAccountTypeEnum.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
            position = dataBundle.getInt(KeyValueStrings.ACCOUNT_VIEW_POSITION.getValue(), -1);
        } else {
            NullPointerException e = new NullPointerException("编辑流水时无法读取原有的数据");
            ExceptionHelper.showExceptionDialog(this, e);
        }

        if (savedInstanceState == null) {
            //创建流水编辑Fragment实例（第一次创建界面时）
            if (type == RunningAccountTypeEnum.EXPENSE) {
                runningAccountFragment = new ExpenseFragment();
            } else if (type == RunningAccountTypeEnum.INCOME) {
                runningAccountFragment = new IncomeFragment();
            } else if (type == RunningAccountTypeEnum.TRANSFER) {
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
            runningAccountFragment.setInitData(dataBundle);   //将原本的数据传递给碎片实例
        }
    }

    //初始化视图
    private void initViews() {
        //为按钮设置单击监听器
        findViewById(R.id.cancel_btn).setOnClickListener(this);
        findViewById(R.id.finish_btn).setOnClickListener(this);
        findViewById(R.id.delete_btn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent result2BookKeeping = new Intent();
        int resultCode = ResultCode.RESULT_REJECT.ordinal();  //响应代码

        if (v.getId() == R.id.finish_btn) {
            String error;
            error = runningAccountFragment.verifyInputData();

            //判断是否获取到报错消息（null:无报错，验证通过）
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                return;
            } else {
                resultCode = ResultCode.RESULT_OK.ordinal();
                Bundle dataBundle = getDataAfterEditing();
                result2BookKeeping.putExtras(dataBundle);
            }
        } else if (v.getId() == R.id.delete_btn) {
            resultCode = ResultCode.RESULT_DELETE.ordinal();
            Bundle dataBundle = new Bundle();
            dataBundle.putInt(KeyValueStrings.ACCOUNT_VIEW_POSITION.getValue(), position);
            result2BookKeeping.putExtras(dataBundle);
        } else if (v.getId() == R.id.cancel_btn) {
            //取消按钮实际上不进行任何操作
            setResult(resultCode, result2BookKeeping);
            finish();
            return;
        } else {
            RuntimeException e = new RuntimeException("无法获取正确的按钮ID");
            ExceptionHelper.showExceptionDialog(this, e);
        }

        setResult(resultCode, result2BookKeeping);
        finish();
    }

    /**
     * 获取修改后的数据
     *
     * @return 包含修改后数据的包裹
     */
    @NonNull
    private Bundle getDataAfterEditing() {
        Bundle dataBundle = runningAccountFragment.getInputData();

        dataBundle.putInt(KeyValueStrings.ACCOUNT_VIEW_POSITION.getValue(), position);        //将下标存放至包裹
        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString());       //将种类存放至包裹

        return dataBundle;
    }
}