package com.manager.assistant.ui.others.bottom_sheets.tag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.manager.assistant.R;
import com.manager.assistant.data.data_class.Tag;
import com.manager.assistant.data.data_class.TagGroup;
import com.manager.assistant.databinding.BottomSheetMultiTagSelectBinding;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.ui.others.adapters.SheetTagGroupRecyclerAdapter;
import com.manager.assistant.ui.others.bottom_sheets.BaseBottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MultiTagSelectBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetMultiTagSelectBinding binding;       //绑定的XML视图
    private final CompositeDisposable disposables = new CompositeDisposable();    //订阅列表（便于取消订阅）
    private final List<Long> tagNoList = new ArrayList<>(); //标签编号列表
    private final OnConfirmListener listener;               //确认按钮按下的监听器

    public interface OnConfirmListener {
        void onConfirm(List<Long> tagNoList);
    }

    public MultiTagSelectBottomSheet(OnConfirmListener listener, List<Long> initTagNoList) {
        this.listener = listener;
        tagNoList.addAll(initTagNoList);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetMultiTagSelectBinding.inflate(inflater, container, false);

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
        //标签清除Chip长按监听
        binding.clearTagChip.setOnLongClickListener(v -> {
            binding.tagChipGroup.removeViews(1, binding.tagChipGroup.getChildCount() - 1);
            tagNoList.clear();
            return true;
        });

        //标签选择列表
        SheetTagGroupRecyclerAdapter tagAdapter = new SheetTagGroupRecyclerAdapter(
                (tagNo, tagName) -> {
                    if (!tagNoList.contains(tagNo)) {
                        //将标签编号添加至列表中
                        tagNoList.add(tagNo);

                        //创建Chip实例
                        Chip tagChip = getClosableTagChip(tagName, tagNo);

                        //添加至ChipGroup中
                        binding.tagChipGroup.addView(tagChip);
                    }
                });
        binding.tagGroupRecycler.setAdapter(tagAdapter);
        loadTagGroup(tagAdapter);

        //初始化已选择的标签
        List<Tag> selectedTagList = Tag.getTagByTagNo(tagNoList, requireContext());
        for (Tag tag : selectedTagList) {
            String tagName = tag.getName();
            long tno = tag.getTno();

            Chip tagChip = getClosableTagChip(tagName, tno);
            binding.tagChipGroup.addView(tagChip);
        }

        //清除和完成按钮
        binding.finishBtn.setOnClickListener(v -> {
            listener.onConfirm(tagNoList);
            dismiss();
        });
        binding.clearFilterBtn.setOnClickListener(v -> dismiss());
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
            tagNoList.remove(tag_no);
        });

        return tagChip;
    }
}
