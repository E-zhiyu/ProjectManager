package com.manager.assistant.ui.pages.main.bookkeeping;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;

import com.google.android.material.search.SearchView;
import com.manager.assistant.generic_enums.LogTags;
import com.manager.assistant.ui.pages.main.MainActivity;
import com.manager.assistant.R;
import com.manager.assistant.data.controllers.AccountDataController;
import com.manager.assistant.data.save.preference.SearchHistoryPreference;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.generic_enums.TagString;
import com.manager.assistant.automation.broadcast.BroadcastActions;
import com.manager.assistant.automation.broadcast.bookkeeping.AccountUpdatedReceiver;
import com.manager.assistant.data.classes.running_account.RunningAccountBase;
import com.manager.assistant.data.classes.running_account.TransferRunningAccount;
import com.manager.assistant.databinding.FragmentBookkeepingBinding;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
import com.manager.assistant.helpers.appearence.ColorHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.file.PictureFileHelper;
import com.manager.assistant.ui.others.adapters.SearchHistoryAdapter;
import com.manager.assistant.ui.pages.main.bookkeeping.adapters.AccountAdapter;
import com.manager.assistant.ui.pages.main.bookkeeping.adapters.DateHeaderAdapter;
import com.manager.assistant.ui.sync.account.AccountUpdateReason;
import com.manager.assistant.ui.sync.account.RunningAccountRepository;
import com.manager.assistant.ui.others.bottom_sheets.filter.AccountFilterBottomSheet;
import com.manager.assistant.generic_enums.RequestResultCode;
import com.manager.assistant.ui.pages.main.bookkeeping.fragments.RunningAccountType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BookKeepingFragment extends Fragment implements AccountAdapter.OnViewClickListener {
    private ConcatAdapter adapter;                          //流水列表适配器
    private final Map<String, AdapterContainer> adapterContainerMap = new LinkedHashMap<>();    //适配器引用Map
    private ActivityResultLauncher<Intent> accountAddLauncher, accountModifyLauncher;   //子活动启动器
    private int accountCount;                               //流水记录数量
    private FragmentBookkeepingBinding binding;             //绑定的XML视图
    private AccountUpdatedReceiver accountUpdatedReceiver;  //流水数据更新的广播接收器
    private final CompositeDisposable disposables = new CompositeDisposable();  //订阅列表（便于取消订阅）
    private AccountFilterBottomSheet.FilterSetting filterSetting = new AccountFilterBottomSheet.FilterSetting();    //过滤器设置
    private String searchText = "";                         //搜索文本，用于搜索流水备注
    private SearchView.TransitionListener searchViewTransitionListener;         //SearchView的变化监听器
    private TextWatcher searchTextWatcher;                  //搜索文本变化监听器

    static class AdapterContainer {
        DateHeaderAdapter headerAdapter;
        AccountAdapter accountAdapter;

        public AdapterContainer(DateHeaderAdapter headerAdapter, AccountAdapter accountAdapter) {
            this.headerAdapter = headerAdapter;
            this.accountAdapter = accountAdapter;
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookkeepingBinding.inflate(inflater, container, false);

        initActivityLauncher();
        initViews();
        setupBroadcastReceiver();

        RunningAccountRepository accountRepository = RunningAccountRepository.getInstance();
        accountRepository.getAccountData().observe(
                getViewLifecycleOwner(),
                simpleRunningAccount -> {
                    if (simpleRunningAccount == null) {
                        return;
                    }

                    AccountUpdateReason reason = accountRepository.getUpdateReason();
                    switch (reason) {
                        case CLEAR:
                            //清零计数文本
                            accountCount = 0;
                            refreshAccountNumText();

                            //清除适配器及其引用
                            for (Map.Entry<String, AdapterContainer> entry : adapterContainerMap.entrySet()) {
                                AdapterContainer adapterContainer = entry.getValue();
                                adapter.removeAdapter(adapterContainer.headerAdapter);
                                adapter.removeAdapter(adapterContainer.accountAdapter);
                            }
                            adapterContainerMap.clear();
                            break;
                        case REFRESH:
                            refreshUI();
                            break;
                    }
                }
        );

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        disposables.dispose();

        //取消注册广播接收器
        if (accountUpdatedReceiver != null) {
            requireContext().unregisterReceiver(accountUpdatedReceiver);
        }

        //清除SearchView的监听器，避免内存泄漏
        if (requireActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.binding.searchView.setupWithSearchBar(null);                           //消除与SearchBar的绑定
            mainActivity.binding.searchView.getEditText().setOnEditorActionListener(null);      //销毁搜索监听
            if (searchViewTransitionListener != null) {
                mainActivity.binding.searchView.removeTransitionListener(searchViewTransitionListener);   //销毁transitionListener
            }
            mainActivity.binding.clearHistoryBtn.setOnClickListener(null);                      //销毁清空历史按钮点击监听
            mainActivity.binding.searchHistoryRecycler.setAdapter(null);                        //清除适配器
            mainActivity.binding.searchView.getEditText().removeTextChangedListener(searchTextWatcher); //清除文本变化监听器
        }
    }

    /**
     * 处理流水视图点击事件的方法
     *
     * @param runningAccount 点击的流水数据实例
     */
    @Override
    public void onRunningAccountClick(@NonNull RunningAccountBase runningAccount) {
        PictureFileHelper.clearTempPictureDir(requireContext());    //清理临时图片目录防止残留干扰

        Intent skip2RunningAccountModify = new Intent(requireContext(), RunningAccountModifyActivity.class);
        Bundle dataBundle = new Bundle();

        //获取基本数据
        RunningAccountType type = runningAccount.getType();         //类型
        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString());
        double amount = runningAccount.getAmount();                 //金额
        dataBundle.putDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), amount);
        String remark = runningAccount.getRemark();                 //备注
        dataBundle.putString(KeyValueStrings.ACCOUNT_REMARK.getValue(), remark);
        String datetime = runningAccount.getDatetime();             //日期
        dataBundle.putString(KeyValueStrings.ACCOUNT_DATETIME.getValue(), datetime);
        long rno = runningAccount.getRno();                         //流水编号
        dataBundle.putLong(KeyValueStrings.ACCOUNT_NO.getValue(), rno);

        //获取特殊数据
        if (type == RunningAccountType.TRANSFER) {
            String exportAccount = ((TransferRunningAccount) runningAccount).getExportAccount();  //转出账户
            dataBundle.putString(KeyValueStrings.ACCOUNT_EXPORT.getValue(), exportAccount);
            String importAccount = ((TransferRunningAccount) runningAccount).getImportAccount();  //转入账户
            dataBundle.putString(KeyValueStrings.ACCOUNT_IMPORT.getValue(), importAccount);
        }

        skip2RunningAccountModify.putExtras(dataBundle);
        accountModifyLauncher.launch(skip2RunningAccountModify);
    }

    /**
     * 初始化活动启动器
     */
    private void initActivityLauncher() {
        accountAddLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == RequestResultCode.RESULT_OK.ordinal() && data != null) {
                        onNewAccountAdded(data);
                    }
                }
        );

        accountModifyLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == RequestResultCode.RESULT_DELETE.ordinal() && data != null) {
                        onAccountDeleted(data);
                    } else if (resultCode == RequestResultCode.RESULT_OK.ordinal() && data != null) {
                        onAccountModified(data);
                    }
                }
        );
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        //搜索框和SearchView
        initSearchComponents();

        //添加按钮
        binding.addFloatingBtn.setOnClickListener(v -> {
            PictureFileHelper.clearTempPictureDir(requireContext());    //清理临时图片目录防止残留干扰
            Intent skip2NewRunningAccount = new Intent(requireContext(), RunningAccountAddActivity.class);
            accountAddLauncher.launch(skip2NewRunningAccount);
        });
        AppearanceAnimationHelper.attachMorphAnimation(binding.addFloatingBtn);

        //绑定过滤器按钮的点击监听器
        binding.filterSelectBtn.setOnClickListener(v -> {
            AccountFilterBottomSheet filterBottomSheet = new AccountFilterBottomSheet(
                    filterSetting,
                    setting -> {
                        filterSetting = setting != null ? setting : new AccountFilterBottomSheet.FilterSetting();
                        refreshUI();
                    });
            filterBottomSheet.show(getParentFragmentManager(), TagString.TAG_SELECT_SHEET.getValue());
            filterBottomSheet.setOnDismissListener(() -> binding.filterSelectBtn.setChecked(false));
        });

        //获取颜色资源并设置下拉刷新布局的颜色
        int colorPrimary = ColorHelper.getPrimaryColor(requireContext());
        int colorSecondary = ColorHelper.getSecondaryPrimaryColor(requireContext());
        binding.refreshLayout.setColorSchemeColors(colorPrimary, colorSecondary);
        int colorBackground = ColorHelper.getBackgroundColor(requireContext());
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(colorBackground);

        //流水记录RecyclerView
        adapter = new ConcatAdapter();
        binding.accountRecycler.setAdapter(adapter);
        refreshUI();

        //设置浮动按钮隐藏行为
        AppearanceAnimationHelper.setupFloatingBtnBehaviour(binding.accountRecycler, binding.addFloatingBtn);

        //设置下拉刷新布局的监听器
        binding.refreshLayout.setOnRefreshListener(this::refreshUI);

        //加载流水记录
        refreshUI();
    }

    /**
     * 刷新流水记录列表
     *
     * @param accountListMap 按照日期分类的流水记录数据（k:流水日期，v:该日期内的流水记录列表）
     */
    private void refreshRecyclerView(@NonNull Map<String, List<RunningAccountBase>> accountListMap) {
        //清除适配器及其引用
        for (Map.Entry<String, AdapterContainer> entry : adapterContainerMap.entrySet()) {
            AdapterContainer adapterContainer = entry.getValue();
            adapter.removeAdapter(adapterContainer.headerAdapter);
            adapter.removeAdapter(adapterContainer.accountAdapter);
        }
        adapterContainerMap.clear();

        //根据数据源是否为空显示空状态提示文本
        if (accountListMap.isEmpty()) {
            binding.emptyTipText.setVisibility(View.VISIBLE);
            return;
        } else {
            binding.emptyTipText.setVisibility(View.GONE);
        }

        //添加适配器到RecyclerView中
        for (Map.Entry<String, List<RunningAccountBase>> entry : accountListMap.entrySet()) {
            String date = entry.getKey();
            List<RunningAccountBase> accountList = entry.getValue();

            //实例化适配器并添加到RecyclerView中
            DateHeaderAdapter headerAdapter = new DateHeaderAdapter(date);
            adapter.addAdapter(headerAdapter);
            AccountAdapter accountAdapter = new AccountAdapter(accountList, this);
            adapter.addAdapter(accountAdapter);

            //保存适配器引用
            AdapterContainer container = new AdapterContainer(headerAdapter, accountAdapter);
            adapterContainerMap.put(date, container);
        }
    }

    /**
     * 刷新流水记录数量文本
     */
    private void refreshAccountNumText() {
        binding.accountNumText.setText(String.format(Locale.getDefault(), "显示数量：%d", accountCount));
    }

    /**
     * 刷新UI方法
     */
    private void refreshUI() {
        binding.refreshLayout.setRefreshing(true);
        disposables.add(
                Observable.fromCallable(() -> AccountDataController.loadRunningAccountData(filterSetting, searchText, requireContext()))
                        .subscribeOn(Schedulers.io())               //在IO线程执行查询
                        .observeOn(AndroidSchedulers.mainThread())  //切换到主线程更新 UI
                        .subscribe(
                                accountListMap -> {
                                    //刷新RecyclerView
                                    refreshRecyclerView(accountListMap);

                                    //刷新数量文本
                                    accountCount = accountListMap.size();
                                    refreshAccountNumText();
                                },  //成功回调
                                e -> {
                                    ExceptionHelper.showExceptionDialog(requireContext(), e);
                                    binding.refreshLayout.setRefreshing(false);
                                    binding.accountNumText.setText(R.string.count_infinity);
                                },  //错误处理
                                () -> {
                                    binding.refreshLayout.setRefreshing(false);
                                    binding.addFloatingBtn.show();
                                }
                        )
        );
    }

    /**
     * 初始化SearchView和SearchBar
     */
    private void initSearchComponents() {
        if (requireActivity() instanceof MainActivity) {
            //绑定到SearchBar
            MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.binding.searchView.setupWithSearchBar(binding.remarkSearchBar);

            //实例化搜索历史适配器并推送至SearchView
            SearchHistoryAdapter searchViewAdapter = new SearchHistoryAdapter(keyWord -> {
                //更新搜索词
                searchText = keyWord;
                binding.remarkSearchBar.setText(keyWord);

                //刷新UI
                refreshUI();

                //隐藏SearchView
                mainActivity.binding.searchView.hide();

                //保存搜索关键词
                if (!keyWord.isEmpty()) {
                    List<String> searchHistoryList = SearchHistoryPreference.getHistory(
                            SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                            requireContext()
                    );
                    searchHistoryList.remove(searchText);       //移除已存在的项
                    searchHistoryList.add(0, searchText);   //添加新项
                    if (searchHistoryList.size() > 15) {        //限制15条记录
                        searchHistoryList = searchHistoryList.subList(0, 14);
                    }
                    SearchHistoryPreference.setHistory(
                            SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                            searchHistoryList,
                            requireContext()
                    );
                }
            });
            mainActivity.binding.searchHistoryRecycler.setAdapter(searchViewAdapter);

            //先刷新一下内容，以应对SearchView展开时的界面重建
            List<String> historyList = SearchHistoryPreference.getHistory(
                    SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                    requireContext()
            );
            searchViewAdapter.refreshSearchHistory(historyList);

            //设置显示监听，用于初始化常用词与清空提示词按钮
            searchViewTransitionListener = (searchView, previousState, newState) -> {
                //显示时执行的动作
                if (newState == SearchView.TransitionState.SHOWING) {
                    List<String> searchHistoryList = SearchHistoryPreference.getHistory(
                            SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                            requireContext()
                    );
                    searchViewAdapter.refreshSearchHistory(searchHistoryList);
                }
            };
            mainActivity.binding.searchView.addTransitionListener(searchViewTransitionListener);

            //设置清除搜索历史按钮点击监听
            mainActivity.binding.clearHistoryBtn.setOnClickListener(v -> {
                SearchHistoryPreference.setHistory(
                        SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                        new ArrayList<>(),
                        requireContext()
                );
                searchViewAdapter.refreshSearchHistory(new ArrayList<>());
            });

            //添加文本变化监听器，用于在清空文本后自动清除搜索并刷新视图
            searchTextWatcher = new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0) {
                        searchText = "";
                        binding.remarkSearchBar.setText("");
                        refreshUI();
                    }
                }
            };
            mainActivity.binding.searchView.getEditText().addTextChangedListener(searchTextWatcher);

            //设置搜索监听
            mainActivity.binding.searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //获取输入文本
                    searchText = mainActivity.binding.searchView.getText().toString();

                    //将文本显示在SearchBar上
                    binding.remarkSearchBar.setText(searchText);

                    //保存搜索关键词
                    if (!searchText.isEmpty()) {
                        List<String> searchHistoryList = SearchHistoryPreference.getHistory(
                                SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                                requireContext()
                        );
                        searchHistoryList.remove(searchText);       //移除已存在的项
                        searchHistoryList.add(0, searchText);   //添加新项
                        if (searchHistoryList.size() > 15) {        //限制15条记录
                            searchHistoryList = searchHistoryList.subList(0, 14);
                        }
                        SearchHistoryPreference.setHistory(
                                SearchHistoryPreference.KEY_ACCOUNT_REMARK,
                                searchHistoryList,
                                requireContext()
                        );
                    }

                    //刷新视图
                    refreshUI();

                    //隐藏SearchView
                    mainActivity.binding.searchView.hide();

                    return true;
                } else {
                    return false;
                }
            });
        }
    }

    /**
     * 初始化广播接收器
     */
    private void setupBroadcastReceiver() {
        accountUpdatedReceiver = new AccountUpdatedReceiver(this::onNewAccountAdded);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastActions.ACTION_RUNNING_ACCOUNT_UPDATED.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(accountUpdatedReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            requireContext().registerReceiver(accountUpdatedReceiver, filter);
        }
    }

    /**
     * 通过监听广播增加的流水账记录（即通过自动记账方式）
     *
     * @param dataBundle 新增流水记录的数据
     */
    private void onNewAccountAdded(@NonNull Bundle dataBundle) {
        //获取日期
        String dateTime = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        if (dateTime == null) {
            return;
        }
        String date = dateTime.substring(0, 10);

        //获取对应的适配器
        AdapterContainer container = adapterContainerMap.get(date);
        if (container == null) {
            return;
        }

        //通过适配器更新Recycler
        container.accountAdapter.addRunningAccount(dataBundle);
        binding.accountRecycler.scrollToPosition(0);
        Toast.makeText(requireContext(), "成功添加一条流水记录（自动记账）", Toast.LENGTH_SHORT).show();

        accountCount++;
        refreshAccountNumText();
    }

    /**
     * 将新建的流水添加至列表视图
     *
     * @param resultIntent 包含流水数据的意图对象
     */
    private void onNewAccountAdded(@NonNull Intent resultIntent) {
        //获取日期
        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            Log.e(LogTags.ACCOUNT_FRAGMENT.getV(), "无法获取流水数据");
            return;
        }
        String dateTime = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        if (dateTime == null) {
            return;
        }
        String date = dateTime.substring(0, 10);

        //获取对应的适配器
        AdapterContainer container = adapterContainerMap.get(date);
        if (container == null) {
            //实例化适配器并添加到界面中
            DateHeaderAdapter headerAdapter = new DateHeaderAdapter(date);
            AccountAdapter accountAdapter = new AccountAdapter(new ArrayList<>(), this);
            adapter.addAdapter(0, accountAdapter);
            adapter.addAdapter(0, headerAdapter);

            //保存引用
            container = new AdapterContainer(headerAdapter, accountAdapter);
            adapterContainerMap.put(date, container);
        }

        //通过适配器更新Recycler
        container.accountAdapter.addRunningAccount(dataBundle);
        binding.accountRecycler.scrollToPosition(0);
        Toast.makeText(requireContext(), "成功添加一条流水记录", Toast.LENGTH_SHORT).show();

        //更新主页报表
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue());
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        RunningAccountRepository accountRepository = RunningAccountRepository.getInstance();
        accountRepository.onAccountUpdated(amount, dateTime, type, AccountUpdateReason.ADD);

        //更新记录数量
        accountCount++;
        refreshAccountNumText();
    }

    /**
     * 用编辑后的流水数据覆盖原来的流水
     *
     * @param resultIntent 带有编辑后数据的意图对象
     */
    private void onAccountModified(@NonNull Intent resultIntent) {
        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            Log.e(LogTags.ACCOUNT_FRAGMENT.getV(), "无法获取流水数据");
            return;
        }
        String oldDateTime = dataBundle.getString(KeyValueStrings.ACCOUNT_OLD_DATETIME.getValue());
        String newDateTime = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        if (newDateTime == null || oldDateTime == null) {
            return;
        }
        String newDate = newDateTime.substring(0, 10);
        String oldDate = oldDateTime.substring(0, 10);

        //通过适配器更新Recycler
        if (!newDate.equals(oldDate)) {
            AdapterContainer newContainer = adapterContainerMap.get(newDate);
            AdapterContainer oldContainer = adapterContainerMap.get(oldDate);
            if (newContainer == null && oldContainer == null) {
                return;
            }

            if (newContainer != null) {
                newContainer.accountAdapter.addRunningAccount(dataBundle);
            }
            if (oldContainer != null) {
                long rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());
                oldContainer.accountAdapter.deleteRunningAccount(rno);

                //如果删除后为空，则删除整个适配器
                if (oldContainer.accountAdapter.getItemCount() == 0) {
                    adapter.removeAdapter(oldContainer.headerAdapter);
                    adapter.removeAdapter(oldContainer.accountAdapter);

                    //移除引用
                    adapterContainerMap.remove(oldDate);
                }
            }
        } else {
            AdapterContainer container = adapterContainerMap.get(newDate);
            if (container == null) {
                return;
            }

            container.accountAdapter.modifyRunningAccount(dataBundle);
        }
        Toast.makeText(requireContext(), "成功修改流水记录", Toast.LENGTH_SHORT).show();

        //更新主页报表
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue());
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        RunningAccountRepository accountRepository = RunningAccountRepository.getInstance();
        accountRepository.onAccountUpdated(amount, newDateTime, type, AccountUpdateReason.MODIFIED);
    }

    /**
     * 删除流水
     */
    private void onAccountDeleted(@NonNull Intent resultIntent) {
        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            Log.e(LogTags.ACCOUNT_FRAGMENT.getV(), "无法获取流水数据");
            return;
        }
        String dateTime = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        if (dateTime == null) {
            return;
        }
        String date = dateTime.substring(0, 10);

        //获取对应的适配器
        AdapterContainer container = adapterContainerMap.get(date);
        if (container == null) {
            return;
        }

        //通过适配器更新Recycler
        long rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue(), -1);
        container.accountAdapter.deleteRunningAccount(rno);
        Toast.makeText(requireContext(), "流水记录已删除", Toast.LENGTH_SHORT).show();

        //如果删除后为空，则删除整个适配器
        if (container.accountAdapter.getItemCount() == 0) {
            adapter.removeAdapter(container.headerAdapter);
            adapter.removeAdapter(container.accountAdapter);

            //移除引用
            adapterContainerMap.remove(date);
        }

        //更新主页报表
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue());
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        RunningAccountRepository accountRepository = RunningAccountRepository.getInstance();
        accountRepository.onAccountUpdated(amount, dateTime, type, AccountUpdateReason.DELETE);

        //更新流水记录数量文本
        accountCount--;
        refreshAccountNumText();
    }
}