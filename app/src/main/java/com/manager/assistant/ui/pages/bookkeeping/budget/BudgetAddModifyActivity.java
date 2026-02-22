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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.manager.assistant.data.data_class.Tag;
import com.manager.assistant.databinding.ActivityBudgetAddModifyBinding;
import com.manager.assistant.enums.KeyValueStrings;
import com.manager.assistant.enums.RequestResultCode;
import com.manager.assistant.enums.TagString;
import com.manager.assistant.helpers.AnimationHelper;
import com.manager.assistant.ui.others.adapters.NoFilteringArrayAdapter;
import com.manager.assistant.ui.others.bottom_sheets.tag.MultiTagSelectBottomSheet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BudgetAddModifyActivity extends AppCompatActivity {
    private ActivityBudgetAddModifyBinding binding;                     //绑定的XML视图
    private boolean isModifyMode = false;                               //是否为修改模式
    private final List<Long> tagNoList = new ArrayList<>();             //保存标签编号的列表
    private ResetFrequency resetFrequency = ResetFrequency.EVERY_DAY;   //预算重置频率
    private long bno = 0;                                               //预算编号
    private int viewHolderPosition = -1;                                //编辑模式下ViewHolder的下标

    public enum ResetFrequency {
        EVERY_DAY("每天", 1),
        EVERY_WEEK("每星期", 7),
        EVERY_MONTH("每个月", -1);
        private final String title;     //显示名称
        private final int intervalDays; //间隔时间

        ResetFrequency(String title, int intervalDays) {
            this.title = title;
            this.intervalDays = intervalDays;
        }

        public String getTitle() {
            return title;
        }

        public int getIntervalDays() {
            return intervalDays;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityBudgetAddModifyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        receiveInitData();
        initViews();
        AnimationHelper.setupAllChildMorphAnimation(binding.getRoot());
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

        isModifyMode = true;
        binding.leftAmountLayout.setVisibility(View.VISIBLE);   //显示剩余金额输入框

        //读取并应用数据包中的数据
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
        binding.deleteBtn.setOnClickListener(v -> {
            Intent result2BudgetManage = new Intent();
            Bundle inputData = getInputData();
            result2BudgetManage.putExtras(inputData);
            setResult(RequestResultCode.RESULT_DELETE.ordinal(), result2BudgetManage);
            finish();
        });
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
        String initAmountStr = String.valueOf(binding.initAmountInput.getText());

        if (String.valueOf(binding.budgetNameInput.getText()).isEmpty()) {
            err = "预算名称不能为空";
            binding.budgetNameLayout.setError(err);
        } else if (initAmountStr.isEmpty()) {
            err = "初始金额不能为空";
            binding.initAmountLayout.setError(err);
        } else if (Double.parseDouble(initAmountStr) == 0) {
            err = "初始金额不能为0";
            binding.initAmountLayout.setError(err);
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
        dataBundle.putDouble(KeyValueStrings.LEFT_AMOUNT.getValue(), leftAmount);
        dataBundle.putString(KeyValueStrings.START_DATE.getValue(), startDate);
        dataBundle.putString(KeyValueStrings.BUDGET_RESET_FREQUENCY.getValue(), resetFrequency.toString());
        dataBundle.putLongArray(KeyValueStrings.TAG_NO.getValue(), tagNos);
        if (isModifyMode) {
            dataBundle.putLong(KeyValueStrings.BNO.getValue(), bno);
            dataBundle.putInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), viewHolderPosition);
        }

        return dataBundle;
    }
}