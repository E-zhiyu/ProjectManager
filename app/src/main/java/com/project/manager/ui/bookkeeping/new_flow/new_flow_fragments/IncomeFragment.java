package com.project.manager.ui.bookkeeping.new_flow.new_flow_fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.project.manager.R;

import java.util.Calendar;

public class IncomeFragment extends FlowFragmentBase implements View.OnClickListener {
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
    public String verifyInputData() {
        String warning = null;

        if (((EditText)binding.findViewById(R.id.amount_textedit)).getText().toString().isEmpty()) {
            warning = "请输入金额";
        }

        return warning;
    }
}