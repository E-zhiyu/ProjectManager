package com.project.manager.ui.bookkeeping.flow_modify.flow_fragments;

import android.annotation.SuppressLint;
import android.view.View;

import com.google.android.material.textfield.TextInputEditText;
import com.project.manager.R;

import java.util.Calendar;

public class IncomeFragment extends FlowFragmentBase implements View.OnFocusChangeListener, View.OnClickListener {
    public IncomeFragment() {
        this.name = "收入";  //为碎片命名
        this.type = FlowTypeEnum.INCOME;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_income;
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        String verification_str, error;
        if (!hasFocus) {
            verification_str = String.valueOf(((TextInputEditText) v).getText());
            if (v.getId() == R.id.amount_input) {
                error = "金额不能为空";
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
        super.onClick(v);
    }

    @Override
    public String verifyInputData() {
        return super.verifyInputData();
    }
}