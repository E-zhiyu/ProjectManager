package com.project.manager.ui.bookkeeping.running_account_edit.modify;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.project.manager.R;
import com.project.manager.ResultCode;
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.ExpenseFragment;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountFragmentBase;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountTypeEnum;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.IncomeFragment;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.TransferFragment;
import com.project.manager.ui.bookkeeping.tag.Tag;

public class RunningAccountModifyActivity extends AppCompatActivity implements View.OnClickListener {
    RunningAccountTypeEnum type = null;                     //流水种类
    int position = -1;                                      //流水项目的下标
    private final String FRAGMENT_TAG = "modify_fragment";  //碎片Tag

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
            //创建流水编辑Fragment实例
            RunningAccountFragmentBase runningAccountFragment = null;
            if (type == RunningAccountTypeEnum.EXPENSE) {
                runningAccountFragment = new ExpenseFragment();
            } else if (type == RunningAccountTypeEnum.INCOME) {
                runningAccountFragment = new IncomeFragment();
            } else if (type == RunningAccountTypeEnum.TRANSFER) {
                runningAccountFragment = new TransferFragment();
            }

            //将Fragment添加到布局
            if (runningAccountFragment != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.running_account_edit_fragment_container, runningAccountFragment, this.FRAGMENT_TAG);
                transaction.commit();
                runningAccountFragment.setInitData(dataBundle);   //将原本的数据传递给碎片实例
            }
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
            RunningAccountFragmentBase current_fragment = (RunningAccountFragmentBase) getSupportFragmentManager().findFragmentByTag(this.FRAGMENT_TAG);
            String error;
            if (current_fragment != null) {
                error = current_fragment.verifyInputData();

                //判断是否获取到报错消息（null:无报错，验证通过）
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    resultCode = ResultCode.RESULT_OK.ordinal();
                    Bundle dataBundle = getDataAfterEditing();
                    result2BookKeeping.putExtras(dataBundle);
                }
            } else {
                //无法获取当前活动的碎片则抛出异常
                NullPointerException e = new NullPointerException("无法获取活动的Fragment");
                ExceptionHelper.showExceptionDialog(this, e);
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
     * 获取编辑后的数据
     *
     * @return 包含编辑后数据的包裹
     */
    private Bundle getDataAfterEditing() {
        Bundle dataBundle = new Bundle();
        double amount;
        String remark, date_time, tag_name;

        //获取基本数据
        TextInputEditText amount_input = findViewById(R.id.amount_input);       //金额
        amount = Double.parseDouble(String.valueOf(amount_input.getText()));
        dataBundle.putDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), amount);
        TextInputEditText remark_input = findViewById(R.id.remark_input);       //备注
        remark = String.valueOf(remark_input.getText());
        dataBundle.putString(KeyValueStrings.ACCOUNT_REMARK.getValue(), remark);
        TextInputEditText datetime_input = findViewById(R.id.datetime_input);   //日期
        date_time = String.valueOf(datetime_input.getText());
        dataBundle.putString(KeyValueStrings.ACCOUNT_DATETIME.getValue(), date_time);
        TextInputEditText tag_input = findViewById(R.id.running_account_tag_input);        //标签名称
        tag_name = String.valueOf(tag_input.getText());

        //将标签名称转换为标签编号
        try {
            long tag_no = Tag.nameTransToTno(tag_name, this);
            dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(this, e);
        }

        dataBundle.putInt(KeyValueStrings.ACCOUNT_VIEW_POSITION.getValue(), position);        //将下标存放至包裹
        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString());       //将种类存放至包裹

        //获取特殊信息
        if (type == RunningAccountTypeEnum.TRANSFER) {
            TextInputEditText export_input, import_input;
            String exportAccount, importAccount;
            export_input = findViewById(R.id.export_account_input);    //转出账户
            exportAccount = String.valueOf(export_input.getText());
            dataBundle.putString(KeyValueStrings.ACCOUNT_EXPORT.getValue(), exportAccount);
            import_input = findViewById(R.id.import_account_input);    //转入账户
            importAccount = String.valueOf(import_input.getText());
            dataBundle.putString(KeyValueStrings.ACCOUNT_IMPORT.getValue(), importAccount);
        }

        return dataBundle;
    }
}