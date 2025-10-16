package com.project.manager.ui.bookkeeping.new_flow.new_flow_fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.project.manager.R;
import com.project.manager.ui.bookkeeping.FlowAttributeStrings;

import java.util.Calendar;

public class TransferFragment extends FlowFragmentBase implements View.OnClickListener {
    public TransferFragment() {
        this.name = "转账";  //为碎片命名
        this.type = FlowTypeEnum.TRANSFER;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_transfer;
    }

    @Override
    protected void initViews(View view) {
        view.findViewById(R.id.flow_date_cardview).setOnClickListener(this);  //为日期卡片容器设置单击监听器

        //初始化日期内容
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("DefaultLocale") String dt_string = String.format("%04d-%02d-%02d %02d:%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE));
        TextView dt_textView = view.findViewById(R.id.date_time_textview);
        dt_textView.setText(dt_string);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.flow_date_cardview) {
            showMaterialDateTimePicker();
        }
    }

    @Override
    public void initViewsWhenEditing(Bundle dataBundle) {
        super.initViewsWhenEditing(dataBundle);

        String exportAccount = dataBundle.getString(FlowAttributeStrings.EXPORT);
        String importAccount = dataBundle.getString(FlowAttributeStrings.IMPORT);
        EditText exportAccountView, importAccountView;
        exportAccountView = binding.findViewById(R.id.export_account_edittext); //转出账户
        exportAccountView.setText(exportAccount);
        importAccountView = binding.findViewById(R.id.import_account_edittext); //转入账户
        importAccountView.setText(importAccount);
    }

    /**
     * 获取转出账户
     *
     * @return 转出账户字符串
     */
    public String getExportAccount() {
        EditText editText = binding.findViewById(R.id.export_account_edittext);
        return editText.getText().toString();
    }

    /**
     * 获取转入账户
     *
     * @return 转入账户字符串
     */
    public String getImportAccount() {
        EditText editText = binding.findViewById(R.id.import_account_edittext);
        return editText.getText().toString();
    }

    @Override
    public String verifyInputData() {
        String warning = null;

        if (((EditText)binding.findViewById(R.id.amount_textedit)).getText().toString().isEmpty()) {
            warning = "请输入金额";
        } else if (((EditText)binding.findViewById(R.id.export_account_edittext)).getText().toString().isEmpty()) {
            warning = "请输入转出账户";
        } else if (((EditText) binding.findViewById(R.id.import_account_edittext)).getText().toString().isEmpty()) {
            warning = "请输入转入账户";
        }

        return warning;
    }
}
