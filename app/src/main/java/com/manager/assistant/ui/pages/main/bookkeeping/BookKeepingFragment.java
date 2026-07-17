package com.manager.assistant.ui.pages.main.bookkeeping;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.R;
import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.entities.AccountEntity;
import com.manager.assistant.data.save.db.services.AccountService;
import com.manager.assistant.data.save.preference.SearchHistoryPreference;
import com.manager.assistant.databinding.ViewHolderSeparatorTextChipBinding;
import com.manager.assistant.generic_enums.KeyStrings;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.generic_enums.TagStrings;
import com.manager.assistant.helpers.BackPressedCallbackHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.SearchHelper;
import com.manager.assistant.ui.others.bottom.AccountFilterBottomSheet;
import com.manager.assistant.ui.others.decoration.sticky.StickyHeaderItemDecoration;
import com.manager.assistant.ui.others.viewmodel.AccountFilterViewModel;
import com.manager.assistant.ui.pages.main.MainActivity;
import com.manager.assistant.databinding.FragmentBookkeepingBinding;
import com.manager.assistant.helpers.appearence.AppearanceHelper;
import com.manager.assistant.helpers.file.PictureFileHelper;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BookKeepingFragment extends Fragment {
    private FragmentBookkeepingBinding binding;             //绑定的XML视图
    private final CompositeDisposable disposable = new CompositeDisposable();  //订阅列表（便于取消订阅）
    private BackPressedCallbackHelper backHelper;   //返回手势拦截器
    private BackPressedCallbackHelper.BackHandler searchBackHandler;    //搜索返回处理器

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookkeepingBinding.inflate(inflater, container, false);
        Log.d(LogTags.ACCOUNT_FRAGMENT.n(), "开始创建界面……");

        initViews();
        initBackHandlers();
        observeLiveData();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        disposable.dispose();

        //清除SearchView的监听器，避免内存泄漏
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).getSearchView().setupWithSearchBar(null);
        }
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //初始化搜索视图
        if (requireActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) requireActivity();
            SearchHelper.initSearchComponents(
                    binding.remarkSearchBar,
                    mainActivity.getSearchView(),
                    mainActivity.getSearchHistoryView(),
                    mainActivity.getClearHistoryBtn(),
                    SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                    keyword -> {
                        AccountFilterViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountFilterViewModel.class);
                        viewModel.executeSearch(keyword);
                    },
                    item -> {
                        int id = item.getItemId();
                        if (id == R.id.action_filter_account) {
                            AccountFilterBottomSheet bottomSheet = new AccountFilterBottomSheet();
                            bottomSheet.show(getParentFragmentManager(), TagStrings.ACCOUNT_FILTER_BOTTOM.getTag());
                            return true;
                        }
                        return false;
                    }
            );
        }

        //添加按钮
        binding.addFloatingBtn.setOnClickListener(v -> {
            PictureFileHelper.clearTempPictureDir(requireContext());    //清理临时图片目录防止残留干扰
            Intent skip2NewRunningAccount = new Intent(requireContext(), RunningAccountInputActivity.class);
            startActivity(skip2NewRunningAccount);
        });
        AppearanceHelper.attachMorphAnimation(binding.addFloatingBtn);

        //流水列表
        AccountListAdapter adapter = new AccountListAdapter(
                (entity, anchor) -> {
                    long accountId = entity.getAccountId();
                    Bundle bundle = new Bundle();
                    bundle.putLong(KeyStrings.ACCOUNT_ID.v(), accountId);

                    Intent skip2AccountInput = new Intent(requireContext(), RunningAccountInputActivity.class);
                    skip2AccountInput.putExtras(bundle);
                    startActivity(skip2AccountInput);
                },
                (entity, anchor) -> {
                    PopupMenu popupMenu = new PopupMenu(requireContext(), anchor, Gravity.END);
                    popupMenu.getMenuInflater().inflate(R.menu.menu_account_list_edit, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == R.id.action_delete_account) {
                            deleteAccount(entity);
                            return true;
                        }

                        return false;
                    });

                    popupMenu.show();
                }
        );
        binding.accountRecycler.setAdapter(adapter);
        AccountFilterViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountFilterViewModel.class);
        BookkeepingDb db = BookkeepingDb.getInstance(requireContext());
        disposable.add(viewModel.loadAccountListDataFlowable(db)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        accountList -> {
                            int visibility = accountList.isEmpty() ? View.VISIBLE : View.GONE;
                            binding.emptyText.setVisibility(visibility);

                            adapter.submitList(accountList);
                        },
                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                )
        );
        StickyHeaderItemDecoration<ViewHolderSeparatorTextChipBinding> decoration = new StickyHeaderItemDecoration<>(
                adapter,
                ViewHolderSeparatorTextChipBinding::inflate,
                (binding1, data) -> binding1.separatorText.setText(data)
        );
        binding.accountRecycler.addItemDecoration(decoration);
    }

    /**
     * 初始化返回手势拦截器
     */
    private void initBackHandlers() {
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                backHelper.dispatchBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(backPressedCallback);
        backHelper = new BackPressedCallbackHelper(backPressedCallback);

        //搜索
        searchBackHandler = new BackPressedCallbackHelper.BackHandler() {
            @Override
            public boolean handleBack() {
                AccountFilterViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountFilterViewModel.class);
                viewModel.clearFilter();
                return true;
            }

            @Override
            public int getPriority() {
                return 1;
            }
        };
    }

    /**
     * 观察 ViewModel 中的 LiveData
     */
    private void observeLiveData() {
        AccountFilterViewModel filterViewModel = new ViewModelProvider(requireActivity()).get(AccountFilterViewModel.class);
        filterViewModel.getFilterUpdatedLiveData().observe(getViewLifecycleOwner(), v ->
                setSearchMode(!filterViewModel.isNoFilter())
        );
    }

    /**
     * 删除流水记录
     *
     * @param account 需要删除的流水记录
     */
    private void deleteAccount(AccountEntity account) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_account)
                .setMessage("确认删除该流水记录吗？其包含的媒体文件也会一并删除")
                .setPositiveButton("确定", (dialogInterface, i) ->
                        disposable.add(AccountService.deleteAccount(account, requireContext())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.io())
                                .subscribe(
                                        () -> Toast.makeText(requireContext(), "流水记录已删除", Toast.LENGTH_SHORT).show(),
                                        e -> ExceptionHelper.showExceptionDialog(requireContext(), e)
                                )
                        )
                )
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 设置搜索模式
     *
     * @param isSearchMode 是否为搜索模式
     */
    private void setSearchMode(boolean isSearchMode) {
        if (!isSearchMode) {
            binding.remarkSearchBar.setText("");
            backHelper.unregisterHandler(searchBackHandler);
        } else {
            backHelper.registerHandler(searchBackHandler);
        }
    }
}