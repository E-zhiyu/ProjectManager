package com.project.manager.pages.newflow.fragments;

import android.app.DatePickerDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.project.manager.R;

import java.util.Calendar;

public class TransferFragment extends NewFlowFragmentBase implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
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
        String dt_string = String.format("%d年%d月%d日", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        TextView dt_textView = view.findViewById(R.id.flow_date_textview);
        dt_textView.setText(dt_string);
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
        String dt = String.format("%d年%d月%d日", year, month + 1, dayOfMonth);
        TextView tv = xmlView.findViewById(R.id.flow_date_textview);
        tv.setText(dt);
    }

    /**
     * 获取转出账户
     * @return 转出账户字符串
     */
    public String getExportAccount() {
        EditText editText = xmlView.findViewById(R.id.export_account_edittext);
        return editText.getText().toString();
    }

    /**
     * 获取转入账户
     * @return 转入账户字符串
     */
    public String getImportAccount() {
        EditText editText = xmlView.findViewById(R.id.import_account_edittext);
        return editText.getText().toString();
    }
}
