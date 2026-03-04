package com.manager.assistant.ui.pages.bookkeeping.budget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.R;
import com.manager.assistant.data.data_class.Tag;
import com.manager.assistant.databinding.ActivityBudgetAddModifyBinding;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.generic_enums.TagString;
import com.manager.assistant.helpers.appearence.AnimationHelper;
import com.manager.assistant.ui.others.adapters.NoFilteringArrayAdapter;
import com.manager.assistant.ui.others.bottom_sheets.tag.MultiTagSelectBottomSheet;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BudgetAddModifyActivity extends AppCompatActivity {
    private ActivityBudgetAddModifyBinding binding;                     //绑定的XML视图
    private boolean isModifyMode = false;                               //是否为修改模式
    private final List<Long> tagNoList = new ArrayList<>();             //保存标签编号的列表
    private ResetFrequency resetFrequency = ResetFrequency.EVERY_DAY;   //预算重置频率
    private long bno = 0;                                               //预算编号
    private int viewHolderPosition = -1;                                //编辑模式下ViewHolder的下标

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityBudgetAddModifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        receiveInitData();
        initViews();
        AnimationHelper.setupAllChildMorphAnimation(binding.getRoot());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    /**
     * 接收初始化数据
     */
    private void receiveInitData() {
        Intent intent = getIntent();
        Bundle dataBundle = intent.getExtras();
        if (dataBundle == null) {
            return;
        }

        binding.toolbar.setTitle(R.string.modify_budget);   //修改标题

        isModifyMode = true;
        binding.leftAmountLayout.setVisibility(View.VISIBLE);   //显示剩余金额输入框

        bno = dataBundle.getLong(KeyValueStrings.BNO.getValue());

        String budgetName = dataBundle.getString(KeyValueStrings.BUDGET_NAME.getValue());
        binding.budgetNameInput.setText(budgetName);

        String frequencyStr = dataBundle.getString(KeyValueStrings.BUDGET_RESET_FREQUENCY.getValue());
        resetFrequency = ResetFrequency.valueOf(frequencyStr);

        double initAmount = dataBundle.getDouble(KeyValueStrings.INIT_AMOUNT.getValue());
        binding.initAmountInput.setText(String.valueOf(initAmount));

        double leftAmount = dataBundle.getDouble(KeyValueStrings.LEFT_AMOUNT.getValue());
        binding.leftAmountInput.setText(String.valueOf(leftAmount));

        String startDate = dataBundle.getString(KeyValueStrings.START_DATE.getValue());
        binding.startDateInput.setText(startDate);

        viewHolderPosition = dataBundle.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());

        long[] tagNos = dataBundle.getLongArray(KeyValueStrings.TAG_NO.getValue());
        if (tagNos != null) {
            List<Long> tagNoList = Arrays.stream(tagNos)
                    .boxed()
                    .collect(Collectors.toList());
            this.tagNoList.addAll(tagNoList);
        }
        refreshTagChipGroup();

        //显示删除按钮
        binding.deleteBtn.setVisibility(View.VISIBLE);
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //标题栏
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        //预算名称
        binding.budgetNameInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.budgetNameLayout.setError(null);
            } else {
                String name = String.valueOf(binding.budgetNameInput.getText());
                if (name.isEmpty()) {
                    binding.budgetNameLayout.setError("预算名称不能为空");
                }
            }
        });

        //初始金额
        binding.initAmountInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.initAmountLayout.setError(null);
            } else {
                String amount = String.valueOf(binding.initAmountInput.getText());
                if (amount.isEmpty()) {
                    binding.initAmountLayout.setError("初始金额不能为空");
                } else if (Double.parseDouble(amount) == 0) {
                    binding.initAmountLayout.setError("初始金额不能为0");
                }
            }
        });

        //日期选择
        binding.startDateInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDateSelectDialog();
            }
        });
        binding.startDateInput.setOnClickListener(v -> showDateSelectDialog());

        //重置频率
        binding.resetFrequencyInput.setText(resetFrequency.getTitle());
        String[] frequencyTitles = Arrays.stream(ResetFrequency.values())
                .map(ResetFrequency::getTitle)
                .toArray(String[]::new);
        NoFilteringArrayAdapter<String> arrayAdapter = new NoFilteringArrayAdapter<>(this, frequencyTitles);
        binding.resetFrequencyInput.setAdapter(arrayAdapter);
        binding.resetFrequencyInput.setOnItemClickListener(
                (parent, view, position, id) -> resetFrequency = ResetFrequency.values()[position]
        );

        //初始化日期输入项
        if (!isModifyMode) {
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            binding.startDateInput.setText(formatter.format(now));
        }

        //标签选择Chip
        binding.tagSelectChip.setOnClickListener(v -> showTagSelectBottomSheet());

        //完成按钮
        binding.finishBtn.setOnClickListener(v -> {
            String err = verifyInput();
            if (err != null) {
                return;
            }

            Intent result2BudgetManage = new Intent();
            Bundle inputData = getInputData();
            result2BudgetManage.putExtras(inputData);
            setResult(Activity.RESULT_OK, result2BudgetManage);
            finish();
        });

        //取消按钮
        binding.cancelBtn.setOnClickListener(v -> finish());

        //删除按钮
        binding.deleteBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle("删除预算")
                .setMessage("确认要删除该预算吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    Intent result2BudgetManage = new Intent();
                    Bundle inputData = getInputData();
                    result2BudgetManage.putExtras(inputData);
                    setResult(RequestResultCode.RESULT_DELETE.ordinal(), result2BudgetManage);
                    finish();
                })
                .setNegativeButton("取消", null)
                .show());
    }

    /**
     * 显示标签选择对话框
     */
    private void showTagSelectBottomSheet() {
        MultiTagSelectBottomSheet bottomSheet = new MultiTagSelectBottomSheet(
                tagNoList -> {
                    this.tagNoList.clear();
                    this.tagNoList.addAll(tagNoList);
                    refreshTagChipGroup();
                },
                tagNoList
        );
        bottomSheet.show(getSupportFragmentManager(), TagString.TAG_SELECT_SHEET.getValue());
    }

    /**
     * 显示日期选择对话框
     */
    private void showDateSelectDialog() {
        //初始化日期格式化器
        String datetimeStr = String.valueOf(binding.startDateInput.getText());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(datetimeStr, formatter);

        long dateSelection = date.atStartOfDay(ZoneOffset.UTC)  //MaterialDateTimePicker使用UTC时区
                .toInstant()
                .toEpochMilli();

        //显示日期选择器
        MaterialDatePicker.Builder<Long> dateBuilder = MaterialDatePicker.Builder.datePicker();
        dateBuilder.setTitleText("选择日期");
        MaterialDatePicker<Long> datePicker = dateBuilder
                .setSelection(dateSelection)    //默认选中输入的日期
                .setCalendarConstraints(
                        new CalendarConstraints.Builder()
                                .setValidator(DateValidatorPointBackward.now()) //限制为过去日期
                                .build()
                )
                .build();
        datePicker.show(getSupportFragmentManager(), TagString.DATE_PICKER.getValue());

        datePicker.addOnPositiveButtonClickListener(selection -> {
            //时间戳转换为LocalDate
            LocalDate selectedDate = Instant.ofEpochMilli(selection)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate();
            String dateStr = selectedDate.format(formatter);
            binding.startDateInput.setText(dateStr);
        });
    }

    /**
     * 刷新标签编号Chip
     */
    private void refreshTagChipGroup() {
        binding.tagChipGroup.removeViews(1, binding.tagChipGroup.getChildCount() - 1);
        List<Tag> tagList = Tag.getTagByTagNo(tagNoList, this);
        for (Tag tag : tagList) {
            String tagName = tag.getName();
            Chip tagChip = new Chip(this);

            tagChip.setText(tagName);
            binding.tagChipGroup.addView(tagChip);
        }
    }

    /**
     * 校验输入内容
     *
     * @return 错误提示(没有错误则为null)
     */
    @Nullable
    private String verifyInput() {
        String err = null;

        //提取部分输入数据
        String initAmountStr = String.valueOf(binding.initAmountInput.getText());
        String leftAmountStr = String.valueOf(binding.leftAmountInput.getText());
        double initAmount, leftAmount;
        try {
            initAmount = Double.parseDouble(initAmountStr);
        } catch (NumberFormatException e) {
            initAmount = 0;
        }
        try {
            leftAmount = Double.parseDouble(leftAmountStr);
        } catch (NumberFormatException e) {
            leftAmount = 0;
        }

        //进行校验
        if (String.valueOf(binding.budgetNameInput.getText()).isEmpty()) {
            err = "预算名称不能为空";
            binding.budgetNameLayout.setError(err);
        } else if (initAmountStr.isEmpty()) {
            err = "初始金额不能为空";
            binding.initAmountLayout.setError(err);
        } else if (initAmount == 0) {
            err = "初始金额不能为0";
            binding.initAmountLayout.setError(err);
        } else if (leftAmountStr.isEmpty() && isModifyMode) {
            err = "剩余金额不能为空";
            binding.leftAmountLayout.setError(err);
        } else if (leftAmount == 0 && isModifyMode) {
            err = "剩余金额不能为0";
            binding.leftAmountLayout.setError(err);
        } else if (tagNoList.isEmpty()) {
            err = "请选择至少一个标签";
        }

        if (err != null) {
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
        }
        return err;
    }

    /**
     * 获取输入的内容
     *
     * @return 包含输入内容的数据包
     */
    @NonNull
    private Bundle getInputData() {
        Bundle dataBundle = new Bundle();

        String budgetName = String.valueOf(binding.budgetNameInput.getText());
        double initAmount;
        try {
            initAmount = Double.parseDouble(String.valueOf(binding.initAmountInput.getText()));
        } catch (NumberFormatException e) {
            initAmount = 0;
        }
        double leftAmount;
        try {
            leftAmount = Double.parseDouble(String.valueOf(binding.leftAmountInput.getText()));
        } catch (NumberFormatException e) {
            leftAmount = 0;
        }
        String startDate = String.valueOf(binding.startDateInput.getText());
        long[] tagNos = tagNoList.stream()
                .mapToLong(Long::longValue)
                .toArray();

        dataBundle.putString(KeyValueStrings.BUDGET_NAME.getValue(), budgetName);
        dataBundle.putDouble(KeyValueStrings.INIT_AMOUNT.getValue(), initAmount);
        dataBundle.putString(KeyValueStrings.START_DATE.getValue(), startDate);
        dataBundle.putString(KeyValueStrings.BUDGET_RESET_FREQUENCY.getValue(), resetFrequency.toString());
        dataBundle.putLongArray(KeyValueStrings.TAG_NO.getValue(), tagNos);
        if (isModifyMode) {
            dataBundle.putDouble(KeyValueStrings.LEFT_AMOUNT.getValue(), leftAmount);
            dataBundle.putLong(KeyValueStrings.BNO.getValue(), bno);
            dataBundle.putInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), viewHolderPosition);
        }

        return dataBundle;
    }
}