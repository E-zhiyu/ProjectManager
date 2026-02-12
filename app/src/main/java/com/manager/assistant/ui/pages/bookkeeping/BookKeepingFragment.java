package com.manager.assistant.ui.pages.bookkeeping;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.enums.KeyValueStrings;
import com.manager.assistant.enums.TagString;
import com.manager.assistant.broadcast.BroadcastConstants;
import com.manager.assistant.broadcast.RunningAccountUpdatedBroadcastReceiver;
import com.manager.assistant.data.data_class.running_account.RunningAccountBase;
import com.manager.assistant.data.data_class.running_account.TransferRunningAccount;
import com.manager.assistant.databinding.FragmentBookkeepingBinding;
import com.manager.assistant.helpers.AnimationHelper;
import com.manager.assistant.helpers.ColorHelper;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.helpers.PictureHelper;
import com.manager.assistant.ui.others.bottom_sheets.filter.AccountFilterBottomSheet;
import com.manager.assistant.ui.others.listeners.RecyclerScrollHideShowListener;
import com.manager.assistant.ui.pages.bookkeeping.running_account.RunningAccountModifyActivity;
import com.manager.assistant.ui.pages.bookkeeping.running_account.RunningAccountAddActivity;
import com.manager.assistant.enums.RequestResultCode;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;
import com.manager.assistant.ui.data_communication.account_recycler.AccountRecyclerViewModel;

import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BookKeepingFragment extends Fragment {
    private AccountAdapter accountAdapter;                          //流水列表适配器
    private ActivityResultLauncher<Intent> accountAddLauncher, accountModifyLauncher;   //子活动启动器
    private int account_count;                                              //流水记录数量
    private FragmentBookkeepingBinding binding;                             //绑定的XML视图
    private RunningAccountUpdatedBroadcastReceiver accountUpdatedReceiver;  //流水数据更新的广播接收器
    private final CompositeDisposable disposables = new CompositeDisposable();    //订阅列表（便于取消订阅）
    private AccountFilterBottomSheet.FilterSetting filterSetting = new AccountFilterBottomSheet.FilterSetting();    //过滤器设置

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookkeepingBinding.inflate(inflater, container, false);

        initActivityLauncher();
        initViews();
        setupBroadcastReceiver();

        AccountRecyclerViewModel viewModel = new ViewModelProvider(requireActivity()).get(AccountRecyclerViewModel.class);
        viewModel.getDataUpdateTrigger().observe(getViewLifecycleOwner(), trigger -> {
            if (trigger != null && trigger) {
                refreshUI();
            }
        });

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
    }

    /**
     * 处理流水视图点击事件的方法
     *
     * @param runningAccount 点击的流水数据实例
     */
    public void onRunningAccountViewClick(@NonNull RunningAccountBase runningAccount) {
        PictureHelper.clearTempPictureDir(requireContext());    //清理临时图片目录防止残留干扰

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
        //绑定单击按钮监听器
        binding.addFloatingBtn.setOnClickListener(v -> {
            PictureHelper.clearTempPictureDir(requireContext());    //清理临时图片目录防止残留干扰
            Intent skip2NewRunningAccount = new Intent(requireContext(), RunningAccountAddActivity.class);
            accountAddLauncher.launch(skip2NewRunningAccount);
        });
        AnimationHelper.attachMorphAnimation(binding.addFloatingBtn);

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

        //创建列表视图的适配器
        setupAccountAdapter();

        //设置下拉刷新布局的监听器
        binding.refreshLayout.setOnRefreshListener(this::refreshUI);
    }

    /**
     * 初始化广播接收器
     */
    private void setupBroadcastReceiver() {
        accountUpdatedReceiver = new RunningAccountUpdatedBroadcastReceiver(this::onNewAccountAdded);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastConstants.ACTION_RUNNING_ACCOUNT_UPDATED.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(accountUpdatedReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            requireContext().registerReceiver(accountUpdatedReceiver, filter);
        }
    }

    /**
     * 初始化流水视图适配器
     */
    private void setupAccountAdapter() {
        //设置适配器
        accountAdapter = new AccountAdapter(this::onRunningAccountViewClick, requireContext());
        binding.accountRecycler.setAdapter(accountAdapter);

        //设置滚动监听器
        binding.accountRecycler.addOnScrollListener(new RecyclerScrollHideShowListener() {
            @Override
            public void onHide() {
                binding.addFloatingBtn.hide();
            }

            @Override
            public void onShow() {
                binding.addFloatingBtn.show();
            }
        });

        //加载流水记录
        refreshUI();
    }

    /**
     * 通过监听广播增加的流水账记录
     *
     * @param dataBundle 新增流水记录的数据
     */
    private void onNewAccountAdded(@NonNull Bundle dataBundle) {
        accountAdapter.addNewRunningAccountByNotification(dataBundle);
        binding.accountRecycler.scrollToPosition(0);
        Toast.makeText(requireContext(), "成功添加一条流水记录（自动记账）", Toast.LENGTH_SHORT).show();

        account_count++;
        refreshAccountNumText();
    }

    /**
     * 将新建的流水添加至列表视图
     *
     * @param resultIntent 包含流水数据的意图对象
     */
    private void onNewAccountAdded(@NonNull Intent resultIntent) {
        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            NullPointerException e = new NullPointerException("无法获取新建的流水数据");
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            return;
        }

        accountAdapter.addNewRunningAccount(dataBundle);            //将新建的流水视图添加至列表视图适配器
        binding.accountRecycler.scrollToPosition(0);                //滚动到顶部（因为添加的新记录在顶部）
        Toast.makeText(requireContext(), "成功添加一条流水记录", Toast.LENGTH_SHORT).show();

        //更新记录数量
        account_count++;
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
            NullPointerException e = new NullPointerException("无法获取修改后的流水数据");
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            return;
        }

        accountAdapter.modifyRunningAccount(dataBundle);
        Toast.makeText(requireContext(), "成功修改流水记录", Toast.LENGTH_SHORT).show();
    }

    /**
     * 删除流水
     */
    private void onAccountDeleted(@NonNull Intent resultIntent) {
        Bundle dataBundle = resultIntent.getExtras();

        if (dataBundle == null) {
            NullPointerException e = new NullPointerException("无法获取流水记录下标");
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            return;
        }

        long rno = dataBundle.getLong(KeyValueStrings.RNO.getValue(), -1);
        accountAdapter.deleteRunningAccount(rno);
        Toast.makeText(requireContext(), "流水记录已删除", Toast.LENGTH_SHORT).show();

        //更新流水记录数量文本
        account_count--;
        refreshAccountNumText();
    }

    /**
     * 刷新流水记录数量文本
     */
    private void refreshAccountNumText() {
        MaterialTextView accountNumText = binding.accountNumText;
        accountNumText.setText(String.format(Locale.getDefault(), "显示数量：%d", account_count));
    }

    /**
     * 刷新UI方法
     */
    private void refreshUI() {
        binding.refreshLayout.setRefreshing(true);
        disposables.add(
                Observable.fromCallable(() -> RunningAccountBase.loadRunningAccountData(filterSetting, requireContext()))
                        .subscribeOn(Schedulers.io())               //在IO线程执行查询
                        .observeOn(AndroidSchedulers.mainThread())  //切换到主线程更新 UI
                        .subscribe(
                                refreshedAccount -> {
                                    accountAdapter.refreshRunningAccount(refreshedAccount);
                                    account_count = refreshedAccount.size();
                                    refreshAccountNumText();
                                },  //成功回调
                                e -> ExceptionHelper.showExceptionDialog(requireContext(), e),  //错误处理
                                () -> {
                                    binding.refreshLayout.setRefreshing(false);
                                    binding.addFloatingBtn.show();
                                }
                        )
        );
    }
}