package com.project.manager.ui.bookkeeping.flow_modify.fragments;

import static com.project.manager.ui.bookkeeping.tag.Tag.tagNoTransToName;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.project.manager.R;
import com.project.manager.database.FlowColumns;
import com.project.manager.database.FlowDatabaseHelper;
import com.project.manager.database.FlowTables;
import com.project.manager.ui.bookkeeping.FlowAttributeStrings;
import com.project.manager.ui.bookkeeping.tag.TagSelectBottomSheet;

import java.util.Calendar;

public abstract class FlowFragmentBase extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.date_time_input) {
            showMaterialDateTimePicker();
        } else if (v.getId() == R.id.flow_tag_input) {
            showTagSelectSheet();
        }
    }

    //初始化碎片布局
    protected void initViews(View view) {
        view.findViewById(R.id.amount_input).setOnFocusChangeListener(this);
        view.findViewById(R.id.date_time_input).setOnClickListener(this);
        view.findViewById(R.id.flow_tag_input).setOnClickListener(this);

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

    //验证输入内容
    public String verifyInputData() {
        String error = null;

        if (String.valueOf(((TextInputEditText) binding.findViewById(R.id.amount_input)).getText()).isEmpty()) {
            error = "金额不能为空";
            ((TextInputEditText) binding.findViewById(R.id.amount_input)).setError(error);
        }

        return error;
    }

    /**
     * 编辑流水时初始化控件内容的方法
     *
     * @param dataBundle 包含初始信息的包裹
     */
    public void initViewsWhenEditing(Bundle dataBundle) {
        TextInputEditText amountView, remarkView;
        double amount = dataBundle.getDouble(FlowAttributeStrings.AMOUNT, -1);
        String remark = dataBundle.getString(FlowAttributeStrings.REMARK);
        String date_time = dataBundle.getString(FlowAttributeStrings.DATETIME);
        int tag_no = dataBundle.getInt(FlowAttributeStrings.TAG_NO);
        String tag_name = tagNoTransToName(tag_no, requireContext());

        amountView = binding.findViewById(R.id.amount_input);                       //金额
        amountView.setText(String.valueOf(amount));
        remarkView = binding.findViewById(R.id.remark_input);                       //备注
        remarkView.setText(remark);
        TextInputEditText dateView = binding.findViewById(R.id.date_time_input);    //日期
        dateView.setText(date_time);
        TextInputEditText tagView = binding.findViewById(R.id.flow_tag_input);      //标签
        tagView.setText(tag_name);
    }

    /**
     * 获取流水日期
     *
     * @return 流水日期字符串
     */
    public String getDate() {
        TextInputEditText dateTextView = binding.findViewById(R.id.date_time_input);
        return String.valueOf(dateTextView.getText());
    }

    /**
     * 获取流水备注
     *
     * @return 流水备注字符串
     */
    public String getRemark() {
        TextInputEditText remarkEditText = binding.findViewById(R.id.remark_input);
        return String.valueOf(remarkEditText.getText());
    }

    /**
     * 获取流水金额
     *
     * @return 流水金额
     */
    public double getAmount() {
        TextInputEditText remarkEditText = binding.findViewById(R.id.amount_input);
        return Double.parseDouble(String.valueOf(remarkEditText.getText()));
    }

    /**
     * 获取标签字符串
     *
     * @return 标签字符串
     */
    public String getFlowTag() {
        TextInputEditText tag_input = binding.findViewById(R.id.flow_tag_input);
        return String.valueOf(tag_input.getText());
    }

    /**
     * 弹出日期和时间选择框
     */
    protected void showMaterialDateTimePicker() {
        //创建日期选择器
        MaterialDatePicker.Builder<Long> dateBuilder = MaterialDatePicker.Builder.datePicker();
        dateBuilder.setTitleText("");
        dateBuilder.setTheme(R.style.DatePickerDialogStyle);

        //显示日期选择器
        MaterialDatePicker<Long> datePicker = dateBuilder.build();
        datePicker.show(getParentFragmentManager(), "DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selected_calendar = Calendar.getInstance();
            selected_calendar.setTimeInMillis(selection);

            //选择日期后，再弹出时间选择器
            showTimePicker(selected_calendar);
        });
    }

    /**
     * 显示时间选择对话框
     *
     * @param initialCalendar 包含选择日期信息的日历对象
     */
    private void showTimePicker(Calendar initialCalendar) {
        //创建时间选择器
        MaterialTimePicker.Builder timeBuilder = new MaterialTimePicker.Builder();
        timeBuilder.setTimeFormat(TimeFormat.CLOCK_24H); // 24小时制
        timeBuilder.setHour(initialCalendar.get(Calendar.HOUR_OF_DAY));
        timeBuilder.setMinute(initialCalendar.get(Calendar.MINUTE));
        timeBuilder.setTheme(R.style.TimePickerDialogStyle);
        timeBuilder.setTitleText("");

        //显示时间选择器
        MaterialTimePicker timePicker = timeBuilder.build();
        timePicker.show(getParentFragmentManager(), "TIME_PICKER");

        //监听选择结果
        timePicker.addOnPositiveButtonClickListener(view -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            //组合日期和时间
            initialCalendar.set(Calendar.HOUR_OF_DAY, hour);
            initialCalendar.set(Calendar.MINUTE, minute);

            //修改文本框的日期和时间
            TextInputEditText datetime_input = binding.findViewById(R.id.date_time_input);
            @SuppressLint("DefaultLocale") String datetime_str = String.format("%04d-%02d-%02d %02d:%02d",
                    initialCalendar.get(Calendar.YEAR),
                    initialCalendar.get(Calendar.MONTH) + 1,
                    initialCalendar.get(Calendar.DAY_OF_MONTH),
                    initialCalendar.get(Calendar.HOUR_OF_DAY),
                    initialCalendar.get(Calendar.MINUTE));
            datetime_input.setText(datetime_str);
        });
    }

    //显示标签选择视图
    private void showTagSelectSheet() {
        TagSelectBottomSheet tag_sheet = new TagSelectBottomSheet();
        tag_sheet.show(getParentFragmentManager(), "TagSelectBottomSheet");
    }
}

