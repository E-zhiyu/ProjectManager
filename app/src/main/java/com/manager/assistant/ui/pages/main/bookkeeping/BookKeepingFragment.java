package com.manager.assistant.ui.pages.main.bookkeeping;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.R;
import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.entities.AccountEntity;
import com.manager.assistant.data.save.db.services.AccountService;
import com.manager.assistant.databinding.ViewHolderSeparatorTextChipBinding;
import com.manager.assistant.generic_enums.KeyStrings;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.helpers.ExceptionHelper;
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
            Intent skip2NewRunningAccount = new Intent(requireContext(), RunningAccountInputActivity.class);
            startActivity(skip2NewRunningAccount);
        });
        AppearanceHelper.attachMorphAnimation(binding.addFloatingBtn);

        //TODO:绑定过滤器按钮的点击监听器

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
        StickyHeaderItemDecoration<ViewHolderSeparatorTextChipBinding> decoration = new StickyHeaderItemDecoration<>(
                adapter,
                ViewHolderSeparatorTextChipBinding::inflate,
                (binding1, data) -> binding1.separatorText.setText(data)
        );
        binding.accountRecycler.addItemDecoration(decoration);
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
                        disposables.add(AccountService.deleteAccount(account, requireContext())
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
}