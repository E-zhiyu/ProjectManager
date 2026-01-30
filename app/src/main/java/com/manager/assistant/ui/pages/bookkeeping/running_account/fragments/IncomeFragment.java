package com.manager.assistant.ui.pages.bookkeeping.running_account.fragments;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.manager.assistant.data.data_class.Tag;
import com.manager.assistant.databinding.FragmentIncomeBinding;
import com.manager.assistant.enums.KeyValueStrings;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.ui.data_communication.account_picture.AccountPictureViewModel;

import java.util.Calendar;
import java.util.Locale;

public class IncomeFragment extends RunningAccountFragmentBase<FragmentIncomeBinding> {
    public IncomeFragment() {
        this.type = RunningAccountType.INCOME;
    }

    @Override
    protected void setDefaultRemark() {
        this.defaultRemark = "一条收入记录";
    }

    @Override
    protected FragmentIncomeBinding getViewBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentIncomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initViews() {
        tagLayout = binding.runningAccountTagLayout;
        tagInput = binding.runningAccountTagInput;
        datetimeInput = binding.datetimeInput;
        loadingIndicator = binding.loadingIndicator;
        pictureRecycler = binding.pictureRecycler;
        pictureDeleteBtn = binding.pictureDeleteBtn;

        receiveInitData();  //获取组件的引用后接收初始化数据

        binding.amountInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.amountLayout.setError(null);
            } else {
                String amountStr = String.valueOf(binding.amountInput.getText());
                if (amountStr.isEmpty()) {
                    binding.amountLayout.setError("金额不能为空");
                } else if (Double.parseDouble(amountStr) == 0) {
                    binding.amountLayout.setError("金额不能为0");
                }
            }
        });
        binding.amountInput.setOnClickListener(v -> binding.amountLayout.setError(null));

        binding.datetimeInput.setOnClickListener(v -> showMaterialDateTimePicker());
        binding.datetimeInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showMaterialDateTimePicker();
            }
        });
        tagInput.setOnClickListener(v -> showTagSelectSheet());
        tagInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showTagSelectSheet();
            }
        });

        //初始化日期内容
        Calendar calendar = Calendar.getInstance();
        String dt_string = String.format(
                Locale.getDefault(),
                "%04d-%02d-%02d %02d:%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
        binding.datetimeInput.setText(dt_string);

        //添加图片的按钮
        MaterialButton pictureAddBtn = binding.pictureAdd;
        pictureAddBtn.setOnClickListener(v -> showAddPictureBottomSheet());

        //删除图片按钮
        binding.pictureDeleteBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除图片")
                .setMessage("是否删除选中的图片？此操作会立刻执行并且无法撤回！")
                .setPositiveButton(
                        "确定",
                        (dialog, which) -> {
                            viewModelRefreshPictureEnabled = false; //禁用本实例ViewModel的刷新功能，防止动画重叠
                            AccountPictureViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountPictureViewModel.class);
                            pictureAdapter.deleteSelectedPicture(viewModel);
                        }
                )
                .setNegativeButton("取消", null)
                .show());
    }

    @Override
    public String verifyInputData() {
        String error = null;

        //判断是否输入金额
        String amountStr = String.valueOf(binding.amountInput.getText());
        if (amountStr.isEmpty()) {
            error = "金额不能为空";
            binding.amountLayout.setErrorEnabled(true);
            binding.amountLayout.setError(error);
        } else if (Double.parseDouble(amountStr) == 0) {
            error = "金额不能为0";
            binding.amountLayout.setErrorEnabled(true);
            binding.amountLayout.setError(error);
        }

        return error;
    }

    @Override
    protected void receiveInitData() {
        Bundle dataBundle = requireActivity().getIntent().getExtras();
        if (dataBundle == null) {
            return;
        }

        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());

        String tag_name = "";
        try {
            Tag tag = Tag.getTagByRno(rno, requireContext());
            tno = tag.getTno();
            tag_name = tag.getName();
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            Toast.makeText(requireContext(), "无法加载该流水记录的标签信息", Toast.LENGTH_SHORT).show();
        }

        binding.amountInput.setText(String.valueOf(amount));                            //金额
        TextInputEditText remarkInput = binding.remarkInput;                            //备注
        remarkInput.setText(remark);
        MaterialAutoCompleteTextView datetimeInput = binding.datetimeInput;             //日期
        datetimeInput.setText(date_time);
        tagInput.setText(tag_name);                                                     //标签名称
    }

    @Override
    public Bundle getInputData() {
        Bundle dataBundle = new Bundle();

        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString()); //种类
        MaterialAutoCompleteTextView dateTimeTextView = binding.datetimeInput;          //日期和时间
        String datetime = String.valueOf(dateTimeTextView.getText());
        dataBundle.putString(KeyValueStrings.ACCOUNT_DATETIME.getValue(), datetime);
        TextInputEditText remarkEditText = binding.remarkInput;                         //备注
        String remark = String.valueOf(remarkEditText.getText());

        dataBundle.putString(KeyValueStrings.ACCOUNT_REMARK.getValue(), remark);
        double amount = Double.parseDouble(String.valueOf(binding.amountInput.getText()));  //金额
        dataBundle.putDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), amount);
        dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tno);                  //标签编号

        return dataBundle;
    }
}