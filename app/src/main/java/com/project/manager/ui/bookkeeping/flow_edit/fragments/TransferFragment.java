package com.project.manager.ui.bookkeeping.flow_edit.fragments;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.project.manager.R;
import com.project.manager.ui.bookkeeping.KeyValueStrings;

public class TransferFragment extends FlowFragmentBase {
    private TextInputLayout export_layout, import_layout;   //转出和转入账户的文本框布局管理器
    private TextInputEditText export_input, import_input;   //转出和转入账户的文本框

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
        super.initViews(view);
        export_layout = binding.findViewById(R.id.export_account_layout);
        export_input = binding.findViewById(R.id.export_account_input);
        import_layout = binding.findViewById(R.id.import_account_layout);
        import_input = binding.findViewById(R.id.import_account_input);

        export_input.setOnFocusChangeListener(this);
        import_input.setOnFocusChangeListener(this);
    }

    @Override
    public void initViewsWhenModifying(Bundle dataBundle) {
        super.initViewsWhenModifying(dataBundle);

        String exportAccount = dataBundle.getString(KeyValueStrings.FLOW_EXPORT.getValue());
        String importAccount = dataBundle.getString(KeyValueStrings.FLOW_IMPORT.getValue());

        export_input.setText(exportAccount);   //转出账户
        import_input.setText(importAccount);   //转入账户
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        String edittext_str, error;         //文本框内容和错误提示
        TextInputLayout text_edit_layout;   //被验证的文本框对应的布局管理器
        if (!hasFocus) {
            edittext_str = String.valueOf(((TextInputEditText) v).getText());   //获取待验证组件的文本内容
            if (v.getId() == R.id.amount_input) {
                error = "金额不能为空";
                text_edit_layout = amount_layout;
            } else if (v.getId() == R.id.export_account_input) {
                error = "转出账户不能为空";
                text_edit_layout = export_layout;
            } else if (v.getId() == R.id.import_account_input) {
                error = "转入账户不能为空";
                text_edit_layout = import_layout;
            } else {
                throw new NullPointerException("无法获取有效视图ID");
            }
        } else {
            return;
        }

        if (edittext_str.isEmpty()) {
            text_edit_layout.setErrorEnabled(true);
            text_edit_layout.setError(error);
        } else {
            text_edit_layout.setError(null);
            text_edit_layout.setErrorEnabled(false);
        }
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
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    public String verifyInputData() {
        String error = super.verifyInputData();

        if (error != null) {
            return error;
        } else if (String.valueOf(export_input.getText()).isEmpty()) {
            error = "转出账户不能为空";
            export_layout.setErrorEnabled(true);
            export_layout.setError(error);
        } else if (String.valueOf(import_input.getText()).isEmpty()) {
            error = "转入账户不能为空";
            import_layout.setErrorEnabled(true);
            import_layout.setError(error);
        }

        return error;
    }
}
