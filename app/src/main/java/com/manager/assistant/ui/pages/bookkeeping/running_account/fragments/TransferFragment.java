package com.manager.assistant.ui.pages.bookkeeping.running_account.fragments;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.manager.assistant.R;
import com.manager.assistant.data.data_class.Tag;
import com.manager.assistant.data.data_class.running_account.TransferRunningAccount;
import com.manager.assistant.databinding.FragmentTransferBinding;
import com.manager.assistant.enums.KeyValueStrings;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.ui.data_communication.account_picture.AccountPictureViewModel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class TransferFragment extends RunningAccountFragmentBase<FragmentTransferBinding> {
    public TransferFragment() {
        super(RunningAccountType.TRANSFER, "一条转账记录");
    }

    @Override
    protected FragmentTransferBinding getViewBinding(@NonNull LayoutInflater inflater, ViewGroup container) {
        return FragmentTransferBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initViews() {
        tagLayout = binding.runningAccountTagLayout;
        tagInput = binding.runningAccountTagInput;
        datetimeInput = binding.datetimeInput;
        loadingIndicator = binding.loadingIndicator;
        pictureRecycler = binding.pictureRecycler;
        pictureDeleteBtn = binding.pictureDeleteBtn;

        //初始化日期内容(必须在接收初始化数据之前)
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        binding.datetimeInput.setText(formatter.format(now));

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
        binding.runningAccountTagInput.setOnClickListener(v -> showTagSelectSheet());
        binding.runningAccountTagInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showTagSelectSheet();
            }
        });

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
                            List<Boolean> pictureSelectList = pictureAdapter.getPictureSelectList();
                            AccountPictureViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountPictureViewModel.class);

                            viewModel.updateAdapterStat(false); //使用ViewModel关闭所有适配器的删除模式
                            viewModel.deletePicture(pictureSelectList);     //使用ViewModel删除图片

                            long delete_count = pictureSelectList.stream().filter(e -> e == true).count();
                            if (delete_count != 0) {
                                Toast.makeText(requireContext(), String.format(Locale.getDefault(), "已删除%d张图片", delete_count), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "没有图片被删除", Toast.LENGTH_SHORT).show();
                            }
                        }
                )
                .setNegativeButton("取消", null)
                .show());

        binding.exportAccountInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String str = String.valueOf(binding.exportAccountInput.getText());
                if (str.isEmpty()) {
                    binding.exportAccountLayout.setError("转出账户不能为空");
                }
            } else {
                binding.exportAccountLayout.setError(null);
            }
        });
        binding.exportAccountInput.setOnClickListener(v -> binding.exportAccountLayout.setError(null));

        binding.importAccountInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String str = String.valueOf(binding.importAccountInput.getText());
                if (str.isEmpty()) {
                    binding.importAccountLayout.setError("转入账户不能为空");
                }
            } else {
                binding.importAccountLayout.setError(null);
            }
        });
        binding.importAccountInput.setOnClickListener(v -> binding.importAccountLayout.setError(null));

        HashSet<String> importExportAccountNameSet = TransferRunningAccount.getAllExportOrImportAccounts(requireContext());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.exposed_dropdown_popup_item,
                new ArrayList<>(importExportAccountNameSet)
        );
        binding.exportAccountInput.setAdapter(arrayAdapter);
        binding.importAccountInput.setAdapter(arrayAdapter);
    }

    @Override
    public void receiveInitData() {
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
        binding.runningAccountTagInput.setText(tag_name);                               //标签名称

        String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());
        String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());

        binding.exportAccountInput.setText(exportAccount);   //转出账户
        binding.importAccountInput.setText(importAccount);   //转入账户
    }

    /**
     * 获取输入的数据
     *
     * @return 包含输入数据的Bundle
     */
    @Override
    public Bundle getInputData() {
        Bundle dataBundle = new Bundle();

        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString()); //种类
        MaterialAutoCompleteTextView dateTimeTextView = binding.datetimeInput;          //日期和时间
        String datetime = String.valueOf(dateTimeTextView.getText());
        dataBundle.putString(KeyValueStrings.ACCOUNT_DATETIME.getValue(), datetime);
        TextInputEditText remarkEditText = binding.remarkInput;                             //备注
        String remark = String.valueOf(remarkEditText.getText());

        dataBundle.putString(KeyValueStrings.ACCOUNT_REMARK.getValue(), remark);
        double amount = Double.parseDouble(String.valueOf(binding.amountInput.getText()));  //金额
        dataBundle.putDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), amount);
        dataBundle.putLong(KeyValueStrings.TAG_NO.getValue(), tno);                         //标签编号

        String exportAccount = String.valueOf(binding.exportAccountInput.getText());        //转出账户
        dataBundle.putString(KeyValueStrings.ACCOUNT_EXPORT.getValue(), exportAccount);
        String importAccount = String.valueOf(binding.importAccountInput.getText());        //转入账户
        dataBundle.putString(KeyValueStrings.ACCOUNT_IMPORT.getValue(), importAccount);

        return dataBundle;
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
        } else if (String.valueOf(binding.exportAccountInput.getText()).isEmpty()) {
            error = "转出账户不能为空";
            binding.exportAccountLayout.setErrorEnabled(true);
            binding.exportAccountLayout.setError(error);
        } else if (String.valueOf(binding.importAccountInput.getText()).isEmpty()) {
            error = "转入账户不能为空";
            binding.importAccountLayout.setErrorEnabled(true);
            binding.importAccountLayout.setError(error);
        }

        return error;
    }
}
