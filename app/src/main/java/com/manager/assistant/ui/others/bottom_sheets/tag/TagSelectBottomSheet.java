package com.manager.assistant.ui.others.bottom_sheets.tag;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.manager.assistant.data.controllers.TagGroupDataController;
import com.manager.assistant.databinding.BottomSheetTagSelectBinding;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.ui.others.adapters.SheetTagBtnRecyclerAdapter;
import com.manager.assistant.ui.others.adapters.SheetTagGroupRecyclerAdapter;
import com.manager.assistant.ui.others.bottom_sheets.BaseBottomSheetDialogFragment;
import com.manager.assistant.auxiliary.enums.AccountType;
import com.manager.assistant.ui.pages.tag.TagManageActivity;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TagSelectBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetTagSelectBinding binding;    //绑定的XML视图
    private long exceptedTagNo = 0;               //被排除的标签编号（不会显示）
    private boolean isTagExcepted = false;          //是否存在被排除的标签
    private final SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener tagBtnClickedListener; //标签按钮点击事件的监听器
    private final CompositeDisposable disposables = new CompositeDisposable();    //订阅列表（便于取消订阅）
    private final AccountType tagScopeType;  //标签作用域种类（即流水记录种类）

    /**
     * 标签选择菜单构造方法
     *
     * @param listener 标签按钮点击监听器
     */
    public TagSelectBottomSheet(SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener listener, AccountType tagScopeType) {
        this.tagBtnClickedListener = listener;
        this.tagScopeType = tagScopeType;
    }

    /**
     * 指定排除的标签的构造方法
     *
     * @param listener      标签按钮点击的监听器
     * @param exceptedTagNo 被排除的标签编号
     */
    public TagSelectBottomSheet(SheetTagBtnRecyclerAdapter.OnTagBtnClickedListener listener, long exceptedTagNo) {
        this.tagBtnClickedListener = listener;
        this.exceptedTagNo = exceptedTagNo;
        tagScopeType = null;    //不需要过滤标签作用域
        isTagExcepted = true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetTagSelectBinding.inflate(inflater, container, false);

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
        if (!isTagExcepted) {
            binding.editTagBtn.setOnClickListener(v -> {
                Intent skip2TagEdit = new Intent(requireContext(), TagManageActivity.class);
                startActivity(skip2TagEdit);
                dismiss();
            });
            binding.clearInputBtn.setOnClickListener(v -> {
                tagBtnClickedListener.onTagBtnClicked(0, "");
                dismiss();
            });
        } else {
            binding.editTagBtn.setVisibility(View.GONE);
            binding.clearInputBtn.setVisibility(View.GONE);
        }

        //设置标签列表视图适配器
        SheetTagGroupRecyclerAdapter tagAdapter = new SheetTagGroupRecyclerAdapter(tagBtnClickedListener);
        binding.tagGroupRecycler.setAdapter(tagAdapter);
        loadTagGroupData(tagAdapter);
    }

    /**
     * 加载标签数据
     *
     * @param tagAdapter 显示标签数据的视图适配器
     */
    private void loadTagGroupData(@NonNull SheetTagGroupRecyclerAdapter tagAdapter) {
        disposables.add(
                Observable.fromCallable(() -> TagGroupDataController.loadTagGroup(
                                requireContext(),
                                exceptedTagNo,
                                tagScopeType
                        ))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                tagAdapter::setTagGroupMap,
                                e -> ExceptionHelper.showExceptionDialog(requireContext(), e),
                                () -> binding.loadingIndicator.setVisibility(View.GONE)
                        )
        );
    }
}
