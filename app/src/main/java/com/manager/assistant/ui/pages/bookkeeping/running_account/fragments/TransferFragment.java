package com.manager.assistant.ui.pages.bookkeeping.running_account.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.manager.assistant.R;
import com.manager.assistant.data.data_class.running_account.TransferRunningAccount;
import com.manager.assistant.enums.KeyValueStrings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TransferFragment extends RunningAccountFragmentBase {
    private TextInputLayout export_layout, import_layout;               //转出和转入账户的文本框布局管理器
    private MaterialAutoCompleteTextView export_input, import_input;    //转出和转入账户的文本框

    public TransferFragment() {
        super();
        this.type = RunningAccountType.TRANSFER;
    }

    @Override
    protected void setDefaultRemark() {
        this.defaultRemark = "一条转账记录";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_transfer;
    }

    @Override
    protected void initViews() {
        super.initViews();
        export_layout = contentView.findViewById(R.id.export_account_layout);
        export_input = contentView.findViewById(R.id.export_account_input);
        import_layout = contentView.findViewById(R.id.import_account_layout);
        import_input = contentView.findViewById(R.id.import_account_input);

        export_input.setOnFocusChangeListener(this);
        import_input.setOnFocusChangeListener(this);

        HashSet<String> importExportAccountNameSet = TransferRunningAccount.getAllExportOrImportAccounts(requireContext());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.exposed_dropdown_popup_item,
                new ArrayList<>(importExportAccountNameSet)
        );
        export_input.setAdapter(arrayAdapter);
        import_input.setAdapter(arrayAdapter);
    }

    @Override
    public void initViewsWhenModifying(@NonNull Bundle dataBundle) {
        super.initViewsWhenModifying(dataBundle);

        String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());
        String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());

        export_input.setText(exportAccount);   //转出账户
        import_input.setText(importAccount);   //转入账户
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            String edittextStr, error;          //文本框内容和错误提示
            TextInputLayout errLayout;          //被验证的文本框对应的布局管理器
            if (v instanceof MaterialAutoCompleteTextView) {
                edittextStr = String.valueOf(((MaterialAutoCompleteTextView) v).getText());
            } else if (v instanceof TextInputEditText) {
                edittextStr = String.valueOf(((TextInputEditText) v).getText());
            } else {
                return;
            }

            if (v.getId() == R.id.amount_input) {
                error = "金额不能为空";
                errLayout = amountLayout;
            } else if (v.getId() == R.id.export_account_input) {
                error = "转出账户不能为空";
                errLayout = export_layout;
            } else if (v.getId() == R.id.import_account_input) {
                error = "转入账户不能为空";
                errLayout = import_layout;
            } else {
                return;
            }

            if (edittextStr.isEmpty()) {
                errLayout.setErrorEnabled(true);
                errLayout.setError(error);
            } else {
                errLayout.setError(null);
            }
        } else {
            if (v.getId() == R.id.amount_input) {
                amountLayout.setError(null);
            } else if (v.getId() == R.id.export_account_input) {
                export_layout.setError(null);
            } else if (v.getId() == R.id.import_account_input) {
                import_layout.setError(null);
            }
        }
    }

    /**
     * 获取输入的数据
     *
     * @return 包含输入数据的Bundle
     */
    @Override
    public Bundle getInputData() {
        Bundle dataBundle = super.getInputData();

        String export_account = String.valueOf(export_input.getText()); //转出账户
        dataBundle.putString(KeyValueStrings.ACCOUNT_EXPORT.getValue(), export_account);
        String import_account = String.valueOf(import_input.getText()); //转入账户
        dataBundle.putString(KeyValueStrings.ACCOUNT_IMPORT.getValue(), import_account);

        return dataBundle;
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
