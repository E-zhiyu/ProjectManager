package com.project.manager.ui.bookkeeping.running_account_edit.fragments;

import android.annotation.SuppressLint;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.project.manager.ManagerAssistant;
import com.project.manager.R;
import com.project.manager.exception.ExceptionHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.TagString;
import com.project.manager.ui.view_model.AccountTagModifyID;
import com.project.manager.ui.view_model.AccountTagViewModel;
import com.project.manager.ui.bookkeeping.tag.Tag;
import com.project.manager.ui.bookkeeping.tag.select_sheet.TagSelectBottomSheet;
import com.project.manager.ui.bookkeeping.tag.select_sheet.SheetTagBtnRecyclerAdapter;
import com.project.manager.ui.view_model.TagWithModifyID;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public abstract class RunningAccountFragmentBase extends Fragment implements
        View.OnClickListener, View.OnFocusChangeListener, SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener {
    Bundle initData = null;                                 //初始化控件内容的数据（用于编辑流水记录时）
    View binding;                                           //绑定的XML界面
    protected String name;                                  //碎片名称
    protected RunningAccountType type;                  //流水类型
    protected TextInputLayout amount_layout, tag_layout;    //金额和标签文本框布局管理器
    protected TextInputEditText amount_input, tag_input;    //金额和标签文本输入框
    private long tag_no = 0;                                //用户选择的标签编号（默认无标签则为0）
    private AccountTagViewModel tagViewModel;               //用于更新标签名称的ViewModel

    private TagSelectBottomSheet tag_sheet;                 //底部弹出窗口

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = inflater.inflate(getLayoutResId(), container, false);
        initViews(binding);

        //获取Application中的ViewModel
        ManagerAssistant app = (ManagerAssistant) requireActivity().getApplication();
        tagViewModel = app.getAccountTagViewModel();

        startObserveTag();    //监听标签变化

        //判断是否传递了外部数据，如果传递了则将数据填入对应控件
        if (initData != null) {
            initViewsWhenModifying(initData);
        }


        return binding;
    }

    public String getName() {
        return name;
    }

    public void setInitData(Bundle initData) {
        this.initData = initData;
    }

    protected abstract int getLayoutResId();

    public long getTag_no() {
        return tag_no;
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.datetime_input) {
            showMaterialDateTimePicker();
        } else if (v.getId() == R.id.running_account_tag_input) {
            showTagSelectSheet();
        }
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
            } else {
                NullPointerException e = new NullPointerException("无法获取有效视图ID");
                ExceptionHelper.showExceptionDialog(requireContext(), e);
                return;
            }
        } else {
            //暂无获取焦点时的检测
            return;
        }

        //判断待验证的字符串是否为空
        if (edittext_str.isEmpty()) {
            text_edit_layout.setErrorEnabled(true);
            text_edit_layout.setError(error);
        } else {
            text_edit_layout.setError(null);    //消除错误提示
            text_edit_layout.setErrorEnabled(false);
        }
    }

    @Override
    public void onTagBtnClicked(long tag_no, String tag_name) {
        this.tag_no = tag_no;   //更新全局变量中的标签编号

        tag_input.setText(tag_name);
        tag_layout.setErrorEnabled(false);  //去除错误提示
        tag_layout.setError(null);
        tag_sheet.dismiss();
    }

    //初始化碎片布局
    protected void initViews(@NonNull View view) {
        TextInputEditText dt_input = view.findViewById(R.id.datetime_input);
        amount_layout = binding.findViewById(R.id.amount_layout);
        amount_input = binding.findViewById(R.id.amount_input);
        tag_layout = binding.findViewById(R.id.running_account_tag_layout);
        tag_input = binding.findViewById(R.id.running_account_tag_input);

        amount_input.setOnFocusChangeListener(this);
        dt_input.setOnClickListener(this);
        tag_input.setOnClickListener(this);

        //初始化日期内容
        Calendar calendar = Calendar.getInstance();
        @SuppressLint("DefaultLocale") String dt_string = String.format("%04d-%02d-%02d %02d:%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
        dt_input.setText(dt_string);
    }

    //观察标签数据变化
    private void startObserveTag() {
        tagViewModel.getTag().observe(getViewLifecycleOwner(), tagList -> {
            if (tagList != null) {  //判断是否为调用resetTagValue()方法后传入的null值
                for (TagWithModifyID tag : tagList) {
                    String tag_name = tag.getTag_name();
                    long tag_no = tag.getTag_no();
                    AccountTagModifyID modifyID = tag.getModifyID();

                    if (tag_no == this.tag_no) {    //只有找到匹配的标签编号才修改
                        switch (modifyID) {
                            case MODIFY:
                                tag_input.setText(tag_name);
                                break;
                            case DELETE:
                                this.tag_no = 0;
                                tag_input.setText("");
                                break;
                            case MERGE:
                                this.tag_no = Tag.nameTransToTno(tag_name, requireContext());
                                tag_input.setText(tag_name);
                                break;
                        }
                    }
                }
                tagViewModel.resetTagValue();
            }
        });
    }

    //验证输入内容
    public String verifyInputData() {
        String error = null;

        //判断是否输入金额
        if (String.valueOf(amount_input.getText()).isEmpty()) {
            error = "金额不能为空";
            amount_layout.setErrorEnabled(true);
            amount_layout.setError(error);
        }

        return error;
    }

    /**
     * 编辑流水时初始化控件内容的方法
     *
     * @param dataBundle 包含初始信息的包裹
     */
    public void initViewsWhenModifying(@NonNull Bundle dataBundle) {
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        long rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());

        String tag_name = "";
        try {
            Tag tag = Tag.getTagByRno(rno, requireContext());
            tag_no = tag.getTno();
            tag_name = tag.getName();
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            Toast.makeText(requireContext(), "无法加载该流水记录的标签信息", Toast.LENGTH_SHORT).show();
        }

        amount_input.setText(String.valueOf(amount));                               //金额
        TextInputEditText remarkView = binding.findViewById(R.id.remark_input);     //备注
        remarkView.setText(remark);
        TextInputEditText dateView = binding.findViewById(R.id.datetime_input);     //日期
        dateView.setText(date_time);
        tag_input.setText(tag_name);                                                //标签名称
    }

    /**
     * 获取输入的数据
     *
     * @return 包含输入数据的Bundle
     */
    public Bundle getInputData() {
        Bundle dataBundle = new Bundle();

        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString());     //种类
        TextInputEditText dateTimeTextView = binding.findViewById(R.id.datetime_input);     //日期和时间
        String date_time = String.valueOf(dateTimeTextView.getText());
        dataBundle.putString(KeyValueStrings.ACCOUNT_DATETIME.getValue(), date_time);
        TextInputEditText remarkEditText = binding.findViewById(R.id.remark_input);         //备注
        String remark = String.valueOf(remarkEditText.getText());
        dataBundle.putString(KeyValueStrings.ACCOUNT_REMARK.getValue(), remark);
        double amount = Double.parseDouble(String.valueOf(amount_input.getText()));         //金额
        dataBundle.putDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), amount);
        dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tag_no);                      //标签编号

        return dataBundle;
    }

    /**
     * 弹出日期和时间选择框
     */
    protected void showMaterialDateTimePicker() {
        //创建日期选择器
        MaterialDatePicker.Builder<Long> dateBuilder = MaterialDatePicker.Builder.datePicker();
        dateBuilder.setTitleText("选择日期");

        //初始化日期格式化器
        TextInputEditText dateTimeInput = binding.findViewById(R.id.datetime_input);
        String input_datetime = String.valueOf(dateTimeInput.getText());
        String pattern = "yyyy-MM-dd HH:mm";    //日期字符串格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime localDateTime = LocalDateTime.parse(input_datetime, formatter);

        // 转换为 java.util.Date
        long epochMillis = localDateTime.atZone(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        Date date = new Date(epochMillis);

        Calendar initialCalendar = Calendar.getInstance();
        initialCalendar.setTime(date);

        //显示日期选择器
        MaterialDatePicker<Long> datePicker = dateBuilder
                .setSelection(initialCalendar.getTimeInMillis())    //默认选中输入的日期
                .build();
        datePicker.show(getParentFragmentManager(), TagString.DATE_PICKER.getValue());

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selected_calendar = Calendar.getInstance();
            selected_calendar.setTimeInMillis(selection);

            //选择日期后，再弹出时间选择器
            showTimePicker(selected_calendar, initialCalendar);
        });
    }

    /**
     * 显示时间选择对话框
     *
     * @param selectionCalendar 包含选择日期信息的日历对象
     * @param initialCalendar   初始化用的日历对象
     */
    private void showTimePicker(@NonNull Calendar selectionCalendar, @NonNull Calendar initialCalendar) {
        //创建时间选择器
        MaterialTimePicker.Builder timeBuilder = new MaterialTimePicker.Builder();
        timeBuilder.setTimeFormat(TimeFormat.CLOCK_24H); // 24小时制
        int init_hour = initialCalendar.get(Calendar.HOUR_OF_DAY);
        timeBuilder.setHour(init_hour);
        int init_minute = initialCalendar.get(Calendar.MINUTE);
        timeBuilder.setMinute(init_minute);
        timeBuilder.setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK);  //默认使用时钟输入模式而不是键盘
        timeBuilder.setTitleText("选择时间");

        //显示时间选择器
        MaterialTimePicker timePicker = timeBuilder.build();
        timePicker.show(getParentFragmentManager(), TagString.TIME_PICKER.getValue());

        //监听选择结果
        timePicker.addOnPositiveButtonClickListener(view -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            //组合日期和时间
            selectionCalendar.set(Calendar.HOUR_OF_DAY, hour);
            selectionCalendar.set(Calendar.MINUTE, minute);

            //修改文本框的日期和时间
            TextInputEditText datetime_input = binding.findViewById(R.id.datetime_input);
            @SuppressLint("DefaultLocale") String datetime_str = String.format("%04d-%02d-%02d %02d:%02d",
                    selectionCalendar.get(Calendar.YEAR),
                    selectionCalendar.get(Calendar.MONTH) + 1,
                    selectionCalendar.get(Calendar.DAY_OF_MONTH),
                    selectionCalendar.get(Calendar.HOUR_OF_DAY),
                    selectionCalendar.get(Calendar.MINUTE));
            datetime_input.setText(datetime_str);
        });
    }

    //显示选择标签的底部弹出视图
    private void showTagSelectSheet() {
        tag_sheet = new TagSelectBottomSheet(this);
        tag_sheet.show(getParentFragmentManager(), TagString.TAG_SELECT_SHEET.getValue());
    }
}

