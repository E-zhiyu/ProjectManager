package com.manager.assistant.ui.others.bottom;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.services.TagService;
import com.manager.assistant.databinding.BottomSheetTagSelectBinding;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.appearence.VisibilityHelper;
import com.manager.assistant.ui.others.adapters.GroupTagSelectAdapter;
import com.manager.assistant.ui.others.viewmodel.TagSelectViewModel;

import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TagSelectBottomSheet extends BaseBottomSheetDialogFragment {
    private BottomSheetTagSelectBinding binding;    //绑定的 XML 布局
    private final CompositeDisposable disposable = new CompositeDisposable();
    private GroupTagSelectAdapter adapter;          //分组标签适配器

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetTagSelectBinding.inflate(inflater, container, false);

        initViews();

        //绑定消失监听器
        setOnDismissListener(() -> {
            TagSelectViewModel viewModel = new ViewModelProvider(requireActivity()).get(TagSelectViewModel.class);
            viewModel.setNeedExecute(true);
        });

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog instanceof BottomSheetDialog) {
            // 1. 捞出系统的底座容器
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);

                // 2. 强行把底座的高度设置为固定值，防止 ViewPager2 高度不同而突变
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                int desiredHeight = (int) (screenHeight * 0.75); // 70% 屏幕高

                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = desiredHeight;
                bottomSheet.setLayoutParams(layoutParams);

                // 3. 配置展开状态：一探头就直接进入完全展开状态，不给它留半折腾的空间
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true); // 往下滑直接关闭，不允许停留在半高状态

                // 4. 设置默认的起跳高度，防止高度坍塌
                behavior.setPeekHeight(desiredHeight);
            }
        }
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
        //分组标签
        initTagGroup();

        //角色添加按钮
        binding.addBtn.setOnClickListener(view -> {
            //TODO:添加标签
        });
    }

    /**
     * 初始化分组标签
     */
    private void initTagGroup() {
        TagSelectViewModel viewModel = new ViewModelProvider(requireActivity()).get(TagSelectViewModel.class);

        //设置适配器
        adapter = new GroupTagSelectAdapter(
                viewModel.getCheckedTagEntitySet().stream()
                        .map(TagEntity::getTagId)
                        .collect(Collectors.toSet()),
                (tag, isChecked, anchor) -> {
                    if (isChecked) {
                        viewModel.getCheckedTagEntitySet().add(tag);
                    } else {
                        viewModel.getCheckedTagEntitySet().remove(tag);
                    }
                }
        );
        binding.fullTagRecycler.setAdapter(adapter);

        //订阅数据
        BookkeepingDb db = BookkeepingDb.getInstance(requireContext());
        disposable.add(TagService.getGroupedTagFlowable(db)
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
}
