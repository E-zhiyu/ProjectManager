package com.project.manager.ui.bookkeeping.flow_edit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.project.manager.R;
import com.project.manager.RequestResultCode;
import com.project.manager.ui.bookkeeping.FlowAttributeStrings;
import com.project.manager.ui.bookkeeping.new_flow.new_flow_fragments.ExpenseFragment;
import com.project.manager.ui.bookkeeping.new_flow.new_flow_fragments.FlowFragmentBase;
import com.project.manager.ui.bookkeeping.new_flow.new_flow_fragments.FlowTypeEnum;
import com.project.manager.ui.bookkeeping.new_flow.new_flow_fragments.IncomeFragment;
import com.project.manager.ui.bookkeeping.new_flow.new_flow_fragments.TransferFragment;

public class FlowEditActivity extends AppCompatActivity implements View.OnClickListener {
    FlowTypeEnum type = null;                               //流水种类
    int position = -1;                                      //流水项目的下标
    private String FRAGMENT_TAG = "flow_edit_fragment";     //碎片Tag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_edit);

        //为按钮设置单击监听器
        findViewById(R.id.cancel_btn).setOnClickListener(this);
        findViewById(R.id.finish_btn).setOnClickListener(this);
        findViewById(R.id.delete_btn).setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        Intent result2BookKeeping = new Intent();
        int resultCode;  //响应代码

        if (v.getId() == R.id.cancel_btn) {
            resultCode = RequestResultCode.RESULT_REJECT.ordinal();
        } else if (v.getId() == R.id.finish_btn) {
            FlowFragmentBase current_fragment = (FlowFragmentBase) getSupportFragmentManager().findFragmentByTag(this.FRAGMENT_TAG);
            String warning = current_fragment.verifyInputData();

            //判断是否获取到警告消息（null:无警告，验证通过）
            if (warning != null) {
                Toast.makeText(this, warning, Toast.LENGTH_SHORT).show();
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
        String remark, date_time;

        //获取基本数据
        EditText amountView = findViewById(R.id.amount_textedit);   //金额
        amount = Double.parseDouble(amountView.getText().toString());
        dataBundle.putDouble(FlowAttributeStrings.AMOUNT, amount);
        EditText remarkView = findViewById(R.id.remark_edittext);   //备注
        remark = remarkView.getText().toString();
        dataBundle.putString(FlowAttributeStrings.REMARK, remark);
        TextView dateView = findViewById(R.id.date_time_textview);       //日期
        date_time = dateView.getText().toString();
        dataBundle.putString(FlowAttributeStrings.DATETIME, date_time);

        dataBundle.putInt(FlowAttributeStrings.POSITION, position);        //将下标存放至包裹
        dataBundle.putString(FlowAttributeStrings.TYPE, type.toString());  //将种类存放至包裹

        //获取特殊信息
        if (type == FlowTypeEnum.TRANSFER) {
            EditText exportView, importView;
            String exportAccount, importAccount;
            exportView = findViewById(R.id.export_account_edittext);    //转出账户
            exportAccount = exportView.getText().toString();
            dataBundle.putString(FlowAttributeStrings.EXPORT, exportAccount);
            importView = findViewById(R.id.import_account_edittext);    //转入账户
            importAccount = importView.getText().toString();
            dataBundle.putString(FlowAttributeStrings.IMPORT, importAccount);
        }

        return dataBundle;
    }
}