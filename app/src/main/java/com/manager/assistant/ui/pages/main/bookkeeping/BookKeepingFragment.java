package com.manager.assistant.ui.pages.main.bookkeeping;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.ui.others.viewmodel.AccountFilterViewModel;
import com.manager.assistant.ui.pages.main.MainActivity;
import com.manager.assistant.databinding.FragmentBookkeepingBinding;
import com.manager.assistant.helpers.appearence.AppearanceHelper;
import com.manager.assistant.helpers.file.PictureFileHelper;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class BookKeepingFragment extends Fragment {
    private FragmentBookkeepingBinding binding;             //绑定的XML视图
    private final CompositeDisposable disposables = new CompositeDisposable();  //订阅列表（便于取消订阅）

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookkeepingBinding.inflate(inflater, container, false);
        Log.d(LogTags.ACCOUNT_FRAGMENT.n(), "开始创建界面……");

        initViews();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        disposables.dispose();

        //清除SearchView的监听器，避免内存泄漏
        if (requireActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) requireActivity();
            if (mainActivity.binding != null) {
                mainActivity.binding.searchView.setupWithSearchBar(null);                           //消除与SearchBar的绑定
                Log.d(LogTags.ACCOUNT_FRAGMENT.n(), "SearchView与SearchBar解绑");
            }
        }
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //TODO:搜索框和SearchView
//        initSearchComponents();

        //添加按钮
        binding.addFloatingBtn.setOnClickListener(v -> {
            PictureFileHelper.clearTempPictureDir(requireContext());    //清理临时图片目录防止残留干扰
            Intent skip2NewRunningAccount = new Intent(requireContext(), RunningAccountAddActivity.class);
            startActivity(skip2NewRunningAccount);
        });
        AppearanceHelper.attachMorphAnimation(binding.addFloatingBtn);

        //TODO:绑定过滤器按钮的点击监听器

        //流水列表
        AccountAdapter adapter = new AccountAdapter(
                (entity, anchor) -> {
                    //TODO:点击监听
                },
                (entity, anchor) -> {
                    //TODO:长按监听
                }
        );
        binding.accountRecycler.setAdapter(adapter);
        AccountFilterViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountFilterViewModel.class);
        BookkeepingDb db = BookkeepingDb.getInstance(requireContext());
        disposables.add(viewModel.loadAccountListDataFlowable(db)
                .subscribe(
                        accountList -> {
                            int visibility = accountList.isEmpty() ? View.VISIBLE : View.GONE;
                            binding.emptyTipText.setVisibility(visibility);

                            adapter.submitList(accountList);
                        },
                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                )
        );
    }

//    /**
//     * 刷新UI方法
//     */
//    private void refreshUI() {
//        long currentTimeMilli = System.currentTimeMillis();
//        if (currentTimeMilli - lastRefreshTimeMilli <= 100) {
//            Log.d(LogTags.ACCOUNT_FRAGMENT.getV(), "间隔时间过短，不刷新界面");
//            return;
//        }
//        lastRefreshTimeMilli = currentTimeMilli;
//
//        Log.d(LogTags.ACCOUNT_FRAGMENT.getV(), "刷新界面中……");
//        binding.refreshLayout.setRefreshing(true);
//
//        //刷新RecyclerView
//        AccountFilterViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountFilterViewModel.class);
//        BookkeepingDb db = BookkeepingDb.getInstance(requireContext());
//        disposables.add(viewModel.loadAccountListDataFlowable(db)
//                .subscribe(
//                        accountList -> {
//                            //TODO:加载流水记录列表
//                        },
//                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
//                )
//        );
//    }

//    /**
//     * 初始化SearchView和SearchBar
//     */
//    private void initSearchComponents() {
//        if (requireActivity() instanceof MainActivity) {
//            //绑定到SearchBar
//            MainActivity mainActivity = (MainActivity) requireActivity();
//            mainActivity.binding.searchView.setupWithSearchBar(binding.remarkSearchBar);
//
//            //观察搜索文本
//            AccountSearchViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountSearchViewModel.class);
//            viewModel.getSearchTextData().observe(
//                    getViewLifecycleOwner(),
//                    keyWord -> {
//                        Log.d(LogTags.ACCOUNT_FRAGMENT.getV(), "搜索文本更新");
//                        binding.remarkSearchBar.setText(keyWord);
//                        refreshUI();
//                    }
//            );
//        }
//    }
}