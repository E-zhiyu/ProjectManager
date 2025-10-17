package com.project.manager.ui.bookkeeping.new_flow.new_flow_fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.project.manager.R;
import com.project.manager.ui.bookkeeping.FlowAttributeStrings;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

public abstract class FlowFragmentBase extends Fragment {
    Bundle initData = null;                         //初始化控件内容的数据（用于编辑流水记录时）
    View binding;                                   //绑定的XML界面
    protected String name;                          //碎片名称
    protected FlowTypeEnum type;                    //流水类型

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = inflater.inflate(getLayoutResId(), container, false);
        initViews(binding);

        //判断是否传递了外部数据，如果传递了则将数据填入对应控件
        if (initData != null) {
            initViewsWhenEditing(initData);
        }

        return binding;
    }

    public String getName() {
        return name;
    }

    public FlowTypeEnum getType() {
        return type;
    }

    public void setInitData(Bundle initData) {
        this.initData = initData;
    }

    protected abstract int getLayoutResId();

    //初始化碎片布局
    protected abstract void initViews(View view);

    //验证输入内容
    public abstract String verifyInputData();

    /**
     * 编辑流水时初始化控件内容的方法
     *
     * @param dataBundle 包含初始信息的包裹
     */
    public void initViewsWhenEditing(Bundle dataBundle) {
        EditText amountView, remarkView;
        double amount = dataBundle.getDouble(FlowAttributeStrings.AMOUNT, -1);
        String remark = dataBundle.getString(FlowAttributeStrings.REMARK);
        String date_time = dataBundle.getString(FlowAttributeStrings.DATETIME);

        amountView = binding.findViewById(R.id.amount_input);        //金额
        amountView.setText(String.valueOf(amount));
        remarkView = binding.findViewById(R.id.remark_input);        //备注
        remarkView.setText(remark);
        TextInputEditText dateView = binding.findViewById(R.id.date_time_input);   //日期
        dateView.setText(date_time);
    }

    /**
     * 获取流水日期
     *
     * @return 流水日期字符串
     */
    public String getDate() {
        TextInputEditText dateTextView = binding.findViewById(R.id.date_time_input);
        return dateTextView.getText().toString();
    }

    /**
     * 获取流水备注
     *
     * @return 流水备注字符串
     */
    public String getRemark() {
        EditText remarkEditText = binding.findViewById(R.id.remark_input);
        return remarkEditText.getText().toString();
    }

    /**
     * 获取流水金额
     *
     * @return 流水金额
     */
    public double getAmount() {
        EditText remarkEditText = binding.findViewById(R.id.amount_input);
        return Double.parseDouble(remarkEditText.getText().toString());
    }

    /**
     * 弹出日期和时间选择框
     */
    protected void showMaterialDateTimePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                (date_view, year, monthOfYear, dayOfMonth) -> {
                    // 日期选择回调
                    TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                            (time_view, hourOfDay, minute, second) -> {
                                // 时间选择回调
                                @SuppressLint("DefaultLocale") String datetime = String.format(
                                        "%04d-%02d-%02d %02d:%02d",
                                        year, monthOfYear + 1, dayOfMonth, hourOfDay, minute
                                );

                                //将选择的日期和时间填入文本框
                                TextInputEditText date_time_view = binding.findViewById(R.id.date_time_input);
                                date_time_view.setText(datetime);
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show(getParentFragmentManager(), "NewFlowTimePicker");
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show(getParentFragmentManager(), "NewFlowDatePicker");
    }
}

