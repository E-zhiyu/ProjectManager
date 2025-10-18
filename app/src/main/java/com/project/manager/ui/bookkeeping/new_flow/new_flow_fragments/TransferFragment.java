package com.project.manager.ui.bookkeeping.new_flow.new_flow_fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;
import com.project.manager.R;
import com.project.manager.ui.bookkeeping.FlowAttributeStrings;

import java.util.Calendar;

public class TransferFragment extends FlowFragmentBase implements View.OnFocusChangeListener, View.OnClickListener {
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
        view.findViewById(R.id.amount_input).setOnFocusChangeListener(this);
        view.findViewById(R.id.date_time_input).setOnClickListener(this);
        view.findViewById(R.id.date_time_input).setFocusable(false);    //日期输入框无法获取焦点
        view.findViewById(R.id.export_account_input).setOnFocusChangeListener(this);
        view.findViewById(R.id.import_account_input).setOnFocusChangeListener(this);

        //初始化日期内容
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("DefaultLocale") String dt_string = String.format("%04d-%02d-%02d %02d:%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE));
        TextInputEditText dt_textView = view.findViewById(R.id.date_time_input);
        dt_textView.setText(dt_string);
    }

    @Override
    public void initViewsWhenEditing(Bundle dataBundle) {
        super.initViewsWhenEditing(dataBundle);

        String exportAccount = dataBundle.getString(FlowAttributeStrings.EXPORT);
        String importAccount = dataBundle.getString(FlowAttributeStrings.IMPORT);
        TextInputEditText exportAccountView, importAccountView;
        exportAccountView = binding.findViewById(R.id.export_account_input); //转出账户
        exportAccountView.setText(exportAccount);
        importAccountView = binding.findViewById(R.id.import_account_input); //转入账户
        importAccountView.setText(importAccount);
    }

    /**
     * 获取转出账户
     *
     * @return 转出账户字符串
     */
    public String getExportAccount() {
        TextInputEditText TextInputEditText = binding.findViewById(R.id.export_account_input);
        return String.valueOf(TextInputEditText.getText());
    }

    /**
     * 获取转入账户
     *
     * @return 转入账户字符串
     */
    public String getImportAccount() {
        TextInputEditText TextInputEditText = binding.findViewById(R.id.import_account_input);
        return String.valueOf(TextInputEditText.getText());
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        String verification_str, error;
        if (!hasFocus) {
            verification_str = String.valueOf(((TextInputEditText) v).getText());
            if (v.getId() == R.id.amount_input) {
                error = "金额不能为空";
            } else if (v.getId() == R.id.export_account_input) {
                error = "转出账户不能为空";
            } else if (v.getId() == R.id.import_account_input) {
                error = "转入账户不能为空";
            } else {
                throw new NullPointerException("验证输入内容时无法获取有效视图ID");
            }
        } else {
            return;
        }

        //判断待验证的字符串是否为空
        if (verification_str.isEmpty()) {
            ((TextInputEditText) v).setError(error);
        } else {
            ((TextInputEditText) v).setError(null); //消除错误提示
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.date_time_input) {
            showMaterialDateTimePicker();
        }
    }

    @Override
    public String verifyInputData() {
        String error = null;

        if (String.valueOf(((TextInputEditText) binding.findViewById(R.id.amount_input)).getText()).isEmpty()) {
            error = "金额不能为空";
            ((TextInputEditText) binding.findViewById(R.id.amount_input)).setError(error);
        } else if (String.valueOf(((TextInputEditText) binding.findViewById(R.id.export_account_input)).getText()).isEmpty()) {
            error = "转出账户不能为空";
            ((TextInputEditText) binding.findViewById(R.id.export_account_input)).setError(error);
        } else if (String.valueOf(((TextInputEditText) binding.findViewById(R.id.import_account_input)).getText()).isEmpty()) {
            error = "转入账户不能为空";
            ((TextInputEditText) binding.findViewById(R.id.import_account_input)).setError(error);
        }

        return error;
    }
}
