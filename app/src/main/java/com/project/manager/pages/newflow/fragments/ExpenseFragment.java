package com.project.manager.pages.newflow.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.project.manager.R;
import com.project.manager.databinding.FragmentBookkeepingBinding;

import java.util.Calendar;

public class ExpenseFragment extends NewFlowFragmentBase implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    public ExpenseFragment() {
        name = "支出";  //为碎片命名
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_expense;
    }

    @Override
    protected void initViews(View view) {
        view.findViewById(R.id.flow_date_cardview).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.flow_date_cardview) {
            Calendar calendar = Calendar.getInstance();  //获取日历实例
            DatePickerDialog dt_dialog = new DatePickerDialog(binding.getRoot().getContext(),
                    R.style.DatePickerDialogStyle,
                    this,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dt_dialog.show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String dt = String.format("%d年%d月%d日", year, month, dayOfMonth);
        TextView tv = xmlView.findViewById(R.id.flow_date_textview);
        tv.setText(dt);
    }
}
