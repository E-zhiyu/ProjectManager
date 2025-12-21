package com.project.manager.ui.bookkeeping;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.broadcast.BroadcastConstants;
import com.project.manager.broadcast.RunningAccountUpdatedBroadcastReceiver;
import com.project.manager.data.data_class.Tag;
import com.project.manager.data.data_class.running_account.ExpenseRunningAccount;
import com.project.manager.data.data_class.running_account.IncomeRunningAccount;
import com.project.manager.data.data_class.running_account.RunningAccountBase;
import com.project.manager.data.data_class.running_account.TransferRunningAccount;
import com.project.manager.data.data_save.database.BookKeepingColumns;
import com.project.manager.data.data_save.database.BookKeepingDatabaseHelper;
import com.project.manager.data.data_save.database.BookKeepingTables;
import com.project.manager.databinding.FragmentBookkeepingBinding;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.ui.bookkeeping.running_account_edit.RunningAccountRecyclerAdapter;
import com.project.manager.ui.bookkeeping.running_account_edit.modify.RunningAccountModifyActivity;
import com.project.manager.ui.bookkeeping.running_account_edit.new_running_account.RunningAccountAddActivity;
import com.project.manager.ui.RequestResultCode;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;
import com.project.manager.ui.bookkeeping.tag.select_sheet.TagSelectBottomSheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookKeepingFragment extends Fragment implements RunningAccountRecyclerAdapter.OnRunningAccountViewClickListener {
    private RunningAccountRecyclerAdapter runningAccountRecyclerAdapter;    //流水列表适配器
    private RecyclerView runningAccountRecyclerView;                        //流水列表视图
    private BookKeepingDatabaseHelper running_account_db_helper;            //流水数据库帮助器
    private ActivityResultLauncher<Intent> runningAccountAddLauncher, modifyRunningAccountLauncher;  //子活动启动器
    private int account_num;                                                //流水记录数量
    private FragmentBookkeepingBinding binding;                             //绑定的XML视图
    private RunningAccountUpdatedBroadcastReceiver accountUpdatedReceiver;  //流水数据更新的广播接收器
    private TagSelectBottomSheet tagSelectBottomSheet;                      //标签选择弹窗
    private long filter_tag_no = 0;                                         //过滤器过滤的标签编号

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookkeepingBinding.inflate(inflater, container, false);

        //实例化数据库帮助器
        running_account_db_helper = new BookKeepingDatabaseHelper(requireContext());

        initActivityLauncher();
        initViews();
        setUpBroadcastReceiver();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        //取消注册广播接收器
        if (accountUpdatedReceiver != null) {
            requireContext().unregisterReceiver(accountUpdatedReceiver);
        }
    }

    //处理流水记录项的点击事件
    @Override
    public void onRunningAccountViewClick(int position, RunningAccountBase runningAccountBase) {
        RunningAccountBase runningAccountView = runningAccountRecyclerAdapter.getItem(position);

        Intent skip2RunningAccountModify = new Intent(requireContext(), RunningAccountModifyActivity.class);
        Bundle dataBundle = new Bundle();

        //获取基本数据
        RunningAccountType type = runningAccountView.getType();         //类型
        dataBundle.putString(KeyValueStrings.ACCOUNT_TYPE.getValue(), type.toString());
        double amount = runningAccountView.getAmount();                 //金额
        dataBundle.putDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), amount);
        String remark = runningAccountView.getRemark();                 //备注
        dataBundle.putString(KeyValueStrings.ACCOUNT_REMARK.getValue(), remark);
        boolean isDefaultRemark = runningAccountView.isDefaultRemark(); //是否使用默认备注
        dataBundle.putBoolean(KeyValueStrings.ACCOUNT_IS_DEFAULT_REMARK.getValue(), isDefaultRemark);
        String date_time = runningAccountView.getDate_time();           //日期
        dataBundle.putString(KeyValueStrings.ACCOUNT_DATETIME.getValue(), date_time);
        long rno = runningAccountView.getRno();                         //流水编号
        dataBundle.putLong(KeyValueStrings.ACCOUNT_NO.getValue(), rno);

        dataBundle.putInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), position);  //将待修改的流水实例下标放入包裹

        //获取特殊数据
        if (type == RunningAccountType.TRANSFER) {
            String exportAccount = ((TransferRunningAccount) runningAccountView).getExportAccount();  //转出账户
            dataBundle.putString(KeyValueStrings.ACCOUNT_EXPORT.getValue(), exportAccount);
            String importAccount = ((TransferRunningAccount) runningAccountView).getImportAccount();  //转入账户
            dataBundle.putString(KeyValueStrings.ACCOUNT_IMPORT.getValue(), importAccount);
        }

        skip2RunningAccountModify.putExtras(dataBundle);
        modifyRunningAccountLauncher.launch(skip2RunningAccountModify);
    }

    /**
     * 从数据库中加载流水视图
     */
    @NonNull
    private List<RunningAccountBase> loadRunningAccountData(long tag_no) {
        SQLiteDatabase db = running_account_db_helper.openReadLink();  //获取读连接

        //判断该标签是否存在
        String tag_name = Tag.tagNoTransToName(tag_no, requireContext());
        if (tag_name.isEmpty() && tag_no != 0) {
            tag_no = 0;
            filter_tag_no = 0;
            binding.filterText.setText("全部");
            Toast.makeText(requireContext(), "标签被删除，已自动清空过滤器", Toast.LENGTH_SHORT).show();
        }

        //生成查询条件
        String selection;
        String[] selectionArgs;
        if (tag_no == 0) {
            selection = null;
            selectionArgs = null;
        } else {
            selection = BookKeepingColumns.TAG_NO + "=?";
            selectionArgs = new String[]{String.valueOf(tag_no)};
        }

        //定义查询光标
        Cursor basic_cursor = db.query(
                BookKeepingTables.BASIC.toString(),
                null,
                selection,
                selectionArgs,
                null,
                null,
                BookKeepingColumns.DATETIME + " DESC," + BookKeepingColumns.RNO + " DESC"
        );

        //查询数据
        List<RunningAccountBase> runningAccountList = new ArrayList<>();
        while (basic_cursor.moveToNext()) {
            //流水编号
            long rno = basic_cursor.getLong(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.RNO.toString()));
            //金额
            double amount = basic_cursor.getDouble(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.AMOUNT.toString()));
            //种类
            RunningAccountType type = RunningAccountType.valueOf(basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.TYPE.toString())));
            //备注
            String remark = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.REMARK.toString()));
            if (remark == null) remark = "";
            //是否使用默认备注
            boolean isDefaultRemark;
            isDefaultRemark = remark.isEmpty();
            //日期和时间
            String datetime = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.DATETIME.toString()));

            RunningAccountBase runningAccountView = null;
            switch (type) {
                case EXPENSE:
                    runningAccountView = new ExpenseRunningAccount(rno, remark, datetime, amount, isDefaultRemark);
                    break;
                case INCOME:
                    runningAccountView = new IncomeRunningAccount(rno, remark, datetime, amount, isDefaultRemark);
                    break;
                case TRANSFER:
                    String[] columns = {BookKeepingColumns.EXPORT.toString(), BookKeepingColumns.IMPORT.toString()};
                    String transfer_selection = BookKeepingColumns.RNO + "=?";
                    String[] transfer_selectionArgs = {String.valueOf(rno)};

                    Cursor transfer_cursor = db.query(
                            BookKeepingTables.TRANSFER.toString(),
                            columns,
                            transfer_selection,
                            transfer_selectionArgs,
                            null,
                            null,
                            null
                    );

                    while (transfer_cursor.moveToNext()) {
                        String exportAccount = transfer_cursor.getString(transfer_cursor.getColumnIndexOrThrow(BookKeepingColumns.EXPORT.toString()));
                        String importAccount = transfer_cursor.getString(transfer_cursor.getColumnIndexOrThrow(BookKeepingColumns.IMPORT.toString()));
                        transfer_cursor.close();
                        runningAccountView = new TransferRunningAccount(rno, remark, datetime, amount, isDefaultRemark, exportAccount, importAccount);
                    }

                    break;
                default:
                    RuntimeException e = new RuntimeException("无法辨别获取到的流水类型");
                    ExceptionHelper.showExceptionDialog(requireContext(), e);
                    break;
            }
            if (runningAccountView != null) {
                runningAccountList.add(runningAccountView);
            }
        }
        basic_cursor.close();
        db.close();

        return runningAccountList;
    }

    //初始化活动启动器
    private void initActivityLauncher() {
        runningAccountAddLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == RequestResultCode.RESULT_OK.ordinal()) {
                        if (data != null) {
                            onNewAccountAdded(data);
                        } else {
                            NullPointerException e = new NullPointerException("无法获取新增流水的数据");
                            ExceptionHelper.showExceptionDialog(requireContext(), e);
                        }
                    }
                }
        );

        modifyRunningAccountLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    int resultCode = result.getResultCode();
                    Intent data = result.getData();

                    if (resultCode == RequestResultCode.RESULT_DELETE.ordinal()) {
                        if (data != null) {
                            onAccountDeleted(data);
                        } else {
                            NullPointerException e = new NullPointerException("无法读取编辑后的流水数据");
                            ExceptionHelper.showExceptionDialog(requireContext(), e);
                        }
                    } else if (resultCode == RequestResultCode.RESULT_OK.ordinal()) {
                        if (data != null) {
                            onAccountModified(data);
                        } else {
                            NullPointerException e = new NullPointerException("无法读取编辑后的流水数据");
                            ExceptionHelper.showExceptionDialog(requireContext(), e);
                        }
                    }
                }
        );
    }

    //初始化视图
    private void initViews() {
        //绑定单击按钮监听器
        binding.runningAccountAddBtn.setOnClickListener(v -> {
            Intent skip2NewRunningAccount = new Intent(requireContext(), RunningAccountAddActivity.class);
            runningAccountAddLauncher.launch(skip2NewRunningAccount);
        });

        binding.filterText.setOnClickListener(v -> {
            tagSelectBottomSheet = new TagSelectBottomSheet(this::onTagBtnClicked, null, "清空过滤");
            tagSelectBottomSheet.show(getParentFragmentManager(), TagString.TAG_SELECT_SHEET.getValue());
        });

        SwipeRefreshLayout refreshLayout = binding.refreshLayout;   //获取下拉刷新布局

        //创建列表视图的适配器
        refreshLayout.setRefreshing(true);
        List<RunningAccountBase> runningAccountList = loadRunningAccountData(filter_tag_no);    //读取流水数据
        runningAccountRecyclerAdapter = new RunningAccountRecyclerAdapter(runningAccountList, this, requireContext());
        runningAccountRecyclerView = binding.runningAccountRecyclerView;
        runningAccountRecyclerView.setAdapter(runningAccountRecyclerAdapter);
        refreshLayout.setRefreshing(false);

        //初始化流水记录数量文本
        account_num = runningAccountList.size();
        MaterialTextView accountNumText = binding.accountNumText;
        accountNumText.setText(String.format(Locale.getDefault(), "显示数量：%d", account_num));

        //设置下拉刷新布局的监听器
        refreshLayout.setOnRefreshListener(() -> {
            List<RunningAccountBase> refreshedAccount = loadRunningAccountData(filter_tag_no);
            runningAccountRecyclerAdapter.refreshRunningAccount(refreshedAccount);
            refreshLayout.setRefreshing(false);

            account_num = refreshedAccount.size();
            refreshAccountNumText();
        });
    }

    //初始化广播接收器
    private void setUpBroadcastReceiver() {
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
     * 通过监听广播增加的流水账记录
     *
     * @param dataBundle 新增流水记录的数据
     */
    private void onNewAccountAdded(Bundle dataBundle) {
        if (dataBundle != null) {
            runningAccountRecyclerAdapter.addNewRunningAccountNoSave(dataBundle);
            runningAccountRecyclerView.scrollToPosition(0);
            Toast.makeText(requireContext(), "成功添加一条流水记录（自动记账）", Toast.LENGTH_SHORT).show();

            account_num++;
            refreshAccountNumText();
        }
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

        runningAccountRecyclerAdapter.addNewRunningAccount(dataBundle);  //将新建的流水视图添加至列表视图适配器
        runningAccountRecyclerView.scrollToPosition(0);                  //滚动到顶部（因为添加的新记录在顶部）
        Toast.makeText(requireContext(), "成功添加一条流水记录", Toast.LENGTH_SHORT).show();

        //更新记录数量
        account_num++;
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

        runningAccountRecyclerAdapter.modifyRunningAccount(dataBundle);
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

        int position = dataBundle.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), -1);
        runningAccountRecyclerAdapter.deleteRunningAccount(position);
        Toast.makeText(requireContext(), "流水记录已删除", Toast.LENGTH_SHORT).show();

        //更新流水记录数量文本
        account_num--;
        refreshAccountNumText();
    }

    //刷新流水记录数量文本
    private void refreshAccountNumText() {
        MaterialTextView accountNumText = binding.accountNumText;
        accountNumText.setText(String.format(Locale.getDefault(), "显示数量：%d", account_num));
    }

    /**
     * 处理标签按钮点击的回调
     *
     * @param tag_no   点击的标签编号
     * @param tag_name 点击的标签名称
     */
    private void onTagBtnClicked(long tag_no, String tag_name) {
        filter_tag_no = tag_no;

        if (tag_no == 0) {
            binding.filterText.setText("全部");
        } else {
            binding.filterText.setText(tag_name);
        }

        List<RunningAccountBase> refreshedAccount = loadRunningAccountData(filter_tag_no);
        runningAccountRecyclerAdapter.refreshRunningAccount(refreshedAccount);
        account_num = refreshedAccount.size();
        refreshAccountNumText();

        loadRunningAccountData(filter_tag_no);
        if (tagSelectBottomSheet != null) {
            tagSelectBottomSheet.dismiss();
            tagSelectBottomSheet = null;
        }
    }
}