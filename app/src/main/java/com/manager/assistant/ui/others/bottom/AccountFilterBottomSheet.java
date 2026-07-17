package com.manager.assistant.ui.others.bottom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.manager.assistant.R;
import com.manager.assistant.auxiliary.classes.CustomDateTimeFormatter;
import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.services.TagService;
import com.manager.assistant.databinding.BottomSheetAccountFilterBinding;
import com.manager.assistant.helpers.DateTimePickerHelper;
import com.manager.assistant.auxiliary.enums.AccountType;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.appearence.VisibilityHelper;
import com.manager.assistant.ui.others.adapters.GroupTagMultiSelectAdapter;
import com.manager.assistant.ui.others.viewmodel.AccountFilterViewModel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AccountFilterBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetAccountFilterBinding binding;            //绑定的XML视图
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAccountFilterBinding.inflate(inflater, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            binding.tagGroupRecycler.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        initViews();

        //设置消失监听器
        setOnDismissListener(() -> {
            AccountFilterViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountFilterViewModel.class);
            viewModel.notifyFilterUpdated();
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        disposable.dispose();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        AccountFilterViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountFilterViewModel.class);

        //日期范围选择按钮
        binding.dateRangeSelectBtn.setOnClickListener(view -> showDateRangeSelectDialog());

        //日期范围清空按钮
        binding.dateRangeClearBtn.setOnClickListener(view -> {
            viewModel.setStart(null);
            viewModel.setEnd(null);
            binding.startDateText.setText(R.string.not_applicable);
            binding.endDateText.setText(R.string.not_applicable);
        });

        //日期范围显示
        LocalDate startDate = viewModel.getStart();
        LocalDate endDate = viewModel.getEnd();
        if (startDate != null) {
            binding.startDateText.setText(startDate.format(CustomDateTimeFormatter.DATE_SLASH));
        }
        if (endDate != null) {
            binding.endDateText.setText(endDate.format(CustomDateTimeFormatter.DATE_SLASH));
        }

        //流水种类 ChipGroup
        Set<Integer> filterTypeSet = viewModel.getFilterTypeSet();
        for (AccountType type : AccountType.values()) {
            //创建Chip实例
            Chip chip = new Chip(requireContext());
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setText(type.getTitle());

            if (filterTypeSet.contains(type.ordinal())) {
                chip.setChecked(true);
            }

            //设置点击监听
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterTypeSet.add(type.ordinal());
                } else {
                    filterTypeSet.remove(type.ordinal());
                }
            });

            //将Chip添加至ChipGroup
            binding.typeChipGroup.addView(chip);
        }

        //无标签的流水记录筛选 Chip
        binding.noTagSelectionChip.setOnCheckedChangeListener((compoundButton, b) ->
                viewModel.setIncludeNoTag(b)
        );

        //分组标签列表
        GroupTagMultiSelectAdapter adapter = new GroupTagMultiSelectAdapter(
                viewModel.getFilterTagSet(),
                (entity, isChecked, anchor) -> {
                    if (isChecked) {
                        viewModel.getFilterTagSet().add(entity.getTagId());
                    } else {
                        viewModel.getFilterTagSet().remove(entity.getTagId());
                    }
                }
        );
        binding.tagGroupRecycler.setAdapter(adapter);
        BookkeepingDb db = BookkeepingDb.getInstance(requireContext());
        disposable.add(TagService.getGroupedTagFlowable(db, 0, null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        modelList -> {
                            VisibilityHelper.toggleVisibilityWithFade(binding.loadingIndicator, false);
                            if (modelList.isEmpty()) {
                                VisibilityHelper.toggleVisibilityWithFade(binding.emptyText, true);
                            } else {
                                binding.emptyText.setVisibility(View.GONE);
                            }

                            adapter.submitList(modelList);
                        },
                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                )
        );
    }

    /**
     * 显示日期范围选择对话框
     */
    private void showDateRangeSelectDialog() {
        AccountFilterViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountFilterViewModel.class);
        DateTimePickerHelper.selectDateRange(
                viewModel.getStart(),
                viewModel.getEnd(),
                getParentFragmentManager(),
                requireContext(),
                selection -> {
                    //起始日期
                    long firstSelection = selection.first;
                    LocalDate startDate = Instant.ofEpochMilli(firstSelection)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate();
                    viewModel.setStart(startDate);
                    binding.startDateText.setText(startDate.format(CustomDateTimeFormatter.DATE_SLASH));

                    //结束日期
                    long secondSelection = selection.second;
                    LocalDate endDate = Instant.ofEpochMilli(secondSelection)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate();
                    viewModel.setEnd(endDate);
                    binding.endDateText.setText(endDate.format(CustomDateTimeFormatter.DATE_SLASH));
                }
        );
    }
}
