package com.project.manager.ui.bookkeeping.flow_modify.edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputEditText;
import com.project.manager.R;
import com.project.manager.RequestResultCode;
import com.project.manager.ui.bookkeeping.FlowAttributeStrings;
import com.project.manager.ui.bookkeeping.flow_modify.fragments.ExpenseFragment;
import com.project.manager.ui.bookkeeping.flow_modify.fragments.FlowFragmentBase;
import com.project.manager.ui.bookkeeping.flow_modify.fragments.FlowTypeEnum;
import com.project.manager.ui.bookkeeping.flow_modify.fragments.IncomeFragment;
import com.project.manager.ui.bookkeeping.flow_modify.fragments.TransferFragment;

public class FlowEditActivity extends AppCompatActivity implements View.OnClickListener {
    FlowTypeEnum type = null;                               //流水种类
    int position = -1;                                      //流水项目的下标
    private final String FRAGMENT_TAG = "flow_edit_fragment";     //碎片Tag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_edit);

        initViews();

        //接收种类和下标参数
        Bundle dataBundle = getIntent().getExtras();
        if (dataBundle != null) {
            type = FlowTypeEnum.valueOf(dataBundle.getString(FlowAttributeStrings.TYPE));
            position = dataBundle.getInt(FlowAttributeStrings.POSITION, -1);
        } else {
            throw new NullPointerException("编辑流水时无法读取原有的数据");
        }

        if (savedInstanceState == null) {
            //创建流水编辑Fragment实例
            FlowFragmentBase flowFragment = null;
            if (type == FlowTypeEnum.EXPENSE) {
                flowFragment = new ExpenseFragment();
            } else if (type == FlowTypeEnum.INCOME) {
                flowFragment = new IncomeFragment();
            } else if (type == FlowTypeEnum.TRANSFER) {
                flowFragment = new TransferFragment();
            }

            //将Fragment添加到布局
            if (flowFragment != null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.flow_edit_fragment_container, flowFragment, this.FRAGMENT_TAG);
                transaction.commit();
                flowFragment.setInitData(dataBundle);   //将原本的数据传递给碎片实例
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
        int resultCode;  //响应代码

        if (v.getId() == R.id.cancel_btn) {
            resultCode = RequestResultCode.RESULT_REJECT.ordinal();
        } else if (v.getId() == R.id.finish_btn) {
            FlowFragmentBase current_fragment = (FlowFragmentBase) getSupportFragmentManager().findFragmentByTag(this.FRAGMENT_TAG);
            String error;
            if (current_fragment != null) {
                error = current_fragment.verifyInputData();
            } else {
                throw new NullPointerException("无法获取活动的Fragment");
            }

            //判断是否获取到报错消息（null:无报错，验证通过）
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                return;
            } else {
                resultCode = RequestResultCode.RESULT_OK.ordinal();
                Bundle dataBundle = getDataAfterEditing();
                result2BookKeeping.putExtras(dataBundle);
            }
        } else if (v.getId() == R.id.delete_btn) {
            resultCode = RequestResultCode.RESULT_DELETE_FLOW.ordinal();
            Bundle dataBundle = new Bundle();
            dataBundle.putInt(FlowAttributeStrings.POSITION, position);
            result2BookKeeping.putExtras(dataBundle);
        } else {
            throw new RuntimeException("无法获取正确的按钮ID");
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
        String remark, date_time, tag;

        //获取基本数据
        TextInputEditText amount_input = findViewById(R.id.amount_input);       //金额
        amount = Double.parseDouble(String.valueOf(amount_input.getText()));
        dataBundle.putDouble(FlowAttributeStrings.AMOUNT, amount);
        TextInputEditText remark_input = findViewById(R.id.remark_input);       //备注
        remark = String.valueOf(remark_input.getText());
        dataBundle.putString(FlowAttributeStrings.REMARK, remark);
        TextInputEditText datetime_input = findViewById(R.id.date_time_input);  //日期
        date_time = String.valueOf(datetime_input.getText());
        dataBundle.putString(FlowAttributeStrings.DATETIME, date_time);
        TextInputEditText tag_input = findViewById(R.id.flow_tag_input);        //标签
        tag = String.valueOf(tag_input.getText());
        dataBundle.putString(FlowAttributeStrings.TAG, tag);

        dataBundle.putInt(FlowAttributeStrings.POSITION, position);             //将下标存放至包裹
        dataBundle.putString(FlowAttributeStrings.TYPE, type.toString());       //将种类存放至包裹

        //获取特殊信息
        if (type == FlowTypeEnum.TRANSFER) {
            TextInputEditText export_input, import_input;
            String exportAccount, importAccount;
            export_input = findViewById(R.id.export_account_input);    //转出账户
            exportAccount = String.valueOf(export_input.getText());
            dataBundle.putString(FlowAttributeStrings.EXPORT, exportAccount);
            import_input = findViewById(R.id.import_account_input);    //转入账户
            importAccount = String.valueOf(import_input.getText());
            dataBundle.putString(FlowAttributeStrings.IMPORT, importAccount);
        }

        return dataBundle;
    }
}