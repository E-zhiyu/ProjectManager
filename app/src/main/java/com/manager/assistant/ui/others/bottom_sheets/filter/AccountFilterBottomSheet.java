package com.manager.assistant.ui.others.bottom_sheets.filter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;

import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.manager.assistant.R;
import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.data.classes.TagGroup;
import com.manager.assistant.databinding.BottomSheetAccountFilterBinding;
import com.manager.assistant.generic_enums.TagString;
import com.manager.assistant.helpers.resourse.ResHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.ui.others.adapters.SheetTagGroupRecyclerAdapter;
import com.manager.assistant.ui.others.bottom_sheets.BaseBottomSheetDialogFragment;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AccountFilterBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetAccountFilterBinding binding;            //绑定的XML视图
    private final FilterSetting setting;                        //用户自定义的过滤器设置
    private final CompositeDisposable disposables = new CompositeDisposable();    //订阅列表（便于取消订阅）
    private final OnFilterApplyListener listener;               //过滤器设置变更监听器

    public static class FilterSetting {
        private final List<Long> selectedTagList = new ArrayList<>();       //选择的标签列表
        private final List<Integer> selectedTypeList = new ArrayList<>();   //选择的种类列表
        private LocalDate startDate, endDate;                               //起止日期

        public List<Long> getSelectedTagList() {
            return selectedTagList;
        }

        public List<Integer> getSelectedTypeList() {
            return selectedTypeList;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void addType(Integer type) {
            selectedTypeList.add(type);
        }

        public void removeType(Integer type) {
            selectedTypeList.remove(type);
        }

        public void addTag(Long tagNo) {
            selectedTagList.add(tagNo);
        }

        public void removeTag(Long tagNo) {
            selectedTagList.remove(tagNo);
        }

        public void clearTag() {
            selectedTagList.clear();
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public void clearCalendar() {
            this.startDate = null;
            this.endDate = null;
        }
    }

    public interface OnFilterApplyListener {
        void onFilterApply(FilterSetting setting);
    }

    public AccountFilterBottomSheet(FilterSetting setting, OnFilterApplyListener listener) {
        this.setting = setting;
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAccountFilterBinding.inflate(inflater, container, false);

        initViews();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        disposables.dispose();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //日期选择文本框
        binding.dateRangeInput.setOnClickListener(v -> showDateRangeSelectDialog());
        binding.dateRangeInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDateRangeSelectDialog();
            }
        });
        binding.dateRangeLayout.setEndIconOnClickListener(v -> {
            binding.dateRangeInput.setText("");
            setting.clearCalendar();
        });
        LocalDate startDate = setting.getStartDate();
        LocalDate endDate = setting.getEndDate();
        if (startDate != null && endDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            String start = formatter.format(startDate);
            String end = formatter.format(endDate);
            String selectedDateRange = String.format(
                    Locale.getDefault(),
                    "%s - %s",
                    start,
                    end
            );
            binding.dateRangeInput.setText(selectedDateRange);
        }

        //流水种类ChipGroup
        String[] accountTypeTitles = Arrays.stream(RunningAccountType.values())
                .map(RunningAccountType::getTitle)
                .toArray(String[]::new);
        int index = 0;
        List<Integer> selectedTypeList = setting.getSelectedTypeList();
        for (String title : accountTypeTitles) {
            //创建Chip实例
            Chip titleChip = new Chip(requireContext());
            titleChip.setCheckable(true);
            titleChip.setCheckedIconVisible(true);
            titleChip.setCheckedIcon(ContextCompat.getDrawable(requireContext(), R.drawable.outline_check_24));
            titleChip.setText(title);

            if (selectedTypeList.contains(index)) {
                titleChip.setChecked(true);
            }

            //设置点击监听
            int finalIndex = index;
            titleChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    setting.addType(finalIndex);
                } else {
                    setting.removeType(finalIndex);
                }
            });
            index++;

            //将Chip添加至ChipGroup
            binding.accountStyleChipGroup.addView(titleChip);
        }

        //标签清除Chip长按监听
        binding.clearTagChip.setOnLongClickListener(v -> {
            binding.tagChipGroup.removeAllViews();
            setting.clearTag();
            return true;
        });

        //无标签Chip点击监听
        binding.noTagChip.setOnClickListener(v -> {
            if (!setting.getSelectedTagList().contains(0L)) {
                setting.addTag(0L);

                Chip noTagChip = getClosableTagChip("无标签", 0);
                binding.tagChipGroup.addView(noTagChip, 0);
            }
        });

        //标签选择列表
        SheetTagGroupRecyclerAdapter tagAdapter = new SheetTagGroupRecyclerAdapter(
                (tag_no, tagName) -> {
                    if (!setting.getSelectedTagList().contains(tag_no)) {
                        //将标签编号添加至列表中
                        setting.addTag(tag_no);

                        //创建Chip实例
                        Chip tagChip = getClosableTagChip(tagName, tag_no);

                        //添加至ChipGroup中
                        binding.tagChipGroup.addView(tagChip);
                    }
                });
        binding.tagGroupRecycler.setAdapter(tagAdapter);
        loadTagGroup(tagAdapter);

        //初始化已选择的标签
        List<Tag> selectedTagList = Tag.getTagByTagNo(setting.getSelectedTagList(), requireContext());
        for (Tag tag : selectedTagList) {
            String tagName = tag.getName();
            long tno = tag.getTno();

            Chip tagChip = getClosableTagChip(tagName, tno);
            binding.tagChipGroup.addView(tagChip);
        }

        //清除和完成按钮
        binding.finishBtn.setOnClickListener(v -> {
            listener.onFilterApply(setting);
            dismiss();
        });
        binding.clearFilterBtn.setOnClickListener(v -> {
            listener.onFilterApply(null);
            dismiss();
        });
    }

    /**
     * 加载标签按钮
     *
     * @param tagAdapter 标签布局的适配器
     */
    private void loadTagGroup(@NonNull SheetTagGroupRecyclerAdapter tagAdapter) {
        disposables.add(
                Observable.fromCallable(() -> TagGroup.loadTagGroups(requireContext()))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                tagAdapter::setTagGroupList,
                                e -> ExceptionHelper.showExceptionDialog(requireContext(), e),
                                () -> binding.loadingIndicator.setVisibility(View.GONE)
                        )
        );
    }

    /**
     * 显示日期范围选择对话框
     */
    private void showDateRangeSelectDialog() {
        MaterialDatePicker.Builder<Pair<Long, Long>> dateBuilder = getDateBuilder();

        MaterialDatePicker<Pair<Long, Long>> dateRangePicker = dateBuilder
                .setTheme(ResHelper.getStyleOrThrow(
                        requireContext(),
                        com.google.android.material.R.attr.materialCalendarTheme
                ))
                .setCalendarConstraints(
                        new CalendarConstraints.Builder()
                                .setValidator(DateValidatorPointBackward.now()) //限制为过去日期
                                .build()
                )
                .build();
        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            long first_selection = selection.first;
            LocalDate startDate = Instant.ofEpochMilli(first_selection)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate();
            setting.setStartDate(startDate);

            long second_selection = selection.second;
            LocalDate endDate = Instant.ofEpochMilli(second_selection)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate();
            setting.setEndDate(endDate);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            String start = formatter.format(startDate);
            String end = formatter.format(endDate);
            String selectedDateRange = String.format(
                    Locale.getDefault(),
                    "%s - %s",
                    start,
                    end
            );
            binding.dateRangeInput.setText(selectedDateRange);
        });

        dateRangePicker.show(getParentFragmentManager(), TagString.DATE_PICKER.getValue());
    }

    @NonNull
    private MaterialDatePicker.Builder<Pair<Long, Long>> getDateBuilder() {
        MaterialDatePicker.Builder<Pair<Long, Long>> dateBuilder = MaterialDatePicker.Builder.dateRangePicker();
        dateBuilder.setTitleText("选择日期范围");

        LocalDate startDate = setting.getStartDate();
        LocalDate endDate = setting.getEndDate();
        if (startDate != null && endDate != null) {
            long startTimeMilli = startDate.atStartOfDay()
                    .toInstant(ZoneOffset.UTC)
                    .toEpochMilli();
            long endTimeMilli = endDate.atStartOfDay()
                    .toInstant(ZoneOffset.UTC)
                    .toEpochMilli();
            dateBuilder.setSelection(new Pair<>(startTimeMilli, endTimeMilli));
        }
        return dateBuilder;
    }

    /**
     * 获取可以关闭的标签Chip
     *
     * @param tagName 标签名称
     * @return 显示标签名称的Chip实例
     */
    @NonNull
    private Chip getClosableTagChip(String tagName, long tag_no) {
        Chip tagChip = new Chip(requireContext());
        tagChip.setText(tagName);
        tagChip.setCloseIconVisible(true);
        tagChip.setCloseIcon(ContextCompat.getDrawable(requireContext(), R.drawable.baseline_clear_24));
        tagChip.setOnCloseIconClickListener(v -> {
            binding.tagChipGroup.removeView(tagChip);
            setting.removeTag(tag_no);
        });

        return tagChip;
    }
}
