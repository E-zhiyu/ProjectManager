package com.project.manager.ui.bookkeeping;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.database.BookKeepingColumns;
import com.project.manager.database.BookKeepingDatabaseHelper;
import com.project.manager.database.BookKeepingTables;
import com.project.manager.databinding.FragmentBookkeepingBinding;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.preference.BookKeepingStartDatePreference;
import com.project.manager.ui.bookkeeping.running_account_edit.modify.RunningAccountModifyActivity;
import com.project.manager.ui.bookkeeping.running_account_edit.new_running_account.RunningAccountAddActivity;
import com.project.manager.RequestResultCode;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;
import com.project.manager.ui.bookkeeping.report.ReportActivity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class BookKeepingFragment extends Fragment implements View.OnClickListener, RunningAccountRecyclerAdapter.OnRunningAccountViewClickListener {
    private RunningAccountRecyclerAdapter runningAccountRecyclerAdapter;    //流水列表适配器
    private RecyclerView runningAccountRecyclerView;                        //流水列表视图
    private BookKeepingDatabaseHelper running_account_db_helper;         //流水数据库帮助器
    private ActivityResultLauncher<Intent> runningAccountAddLauncher, modifyRunningAccountLauncher;  //子活动启动器
    MaterialTextView account_num_text;                                      //流水记录数量文本视图
    private int account_num;                                                //流水记录数量
    private long bookkeeping_days;                                          //记账天数
    private FragmentBookkeepingBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookkeepingBinding.inflate(inflater, container, false);

        //实例化数据库帮助器
        running_account_db_helper = new BookKeepingDatabaseHelper(getActivity());

        initActivityLauncher();
        initViews();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v.getId() == R.id.running_account_add_btn) {    //新建流水
            Intent skip2NewRunningAccount = new Intent(getActivity(), RunningAccountAddActivity.class);
            runningAccountAddLauncher.launch(skip2NewRunningAccount);
        } else if (v.getId() == R.id.report_btn) {          //查看报表
            Intent skip2Report = new Intent(getActivity(), ReportActivity.class);
            startActivity(skip2Report);
        }
    }

    //处理流水记录项的点击事件
    @Override
    public void onRunningAccountViewClick(int position, RunningAccountBase runningAccountBase) {
        RunningAccountBase runningAccountView = runningAccountRecyclerAdapter.getItem(position);

        Intent skip2RunningAccountModify = new Intent(getActivity(), RunningAccountModifyActivity.class);
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
            String exportAccount = ((TransferRunningAccount) runningAccountView).exportAccount;  //转出账户
            dataBundle.putString(KeyValueStrings.ACCOUNT_EXPORT.getValue(), exportAccount);
            String importAccount = ((TransferRunningAccount) runningAccountView).importAccount;  //转入账户
            dataBundle.putString(KeyValueStrings.ACCOUNT_IMPORT.getValue(), importAccount);
        }

        skip2RunningAccountModify.putExtras(dataBundle);
        modifyRunningAccountLauncher.launch(skip2RunningAccountModify);
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
    @SuppressLint("DefaultLocale")
    private void initViews() {
        //绑定单击按钮监听器
        View root = binding.getRoot();
        root.findViewById(R.id.running_account_add_btn).setOnClickListener(this);
        root.findViewById(R.id.report_btn).setOnClickListener(this);

        List<RunningAccountBase> runningAccountList = loadRunningAccountData();     //读取流水数据

        //初始化记账日期和流水记录数量文本
        MaterialTextView bookkeeping_days_text = root.findViewById(R.id.bookkeeping_days_text); //显示记账天数和流水记录数量的文本视图
        account_num_text = root.findViewById(R.id.account_num_text);
        account_num = runningAccountList.size();
        String start_date_str = getBookKeepingStartDate();  //获取开始记账的日期
        if (!start_date_str.isEmpty()) {
            LocalDate startDate = LocalDate.parse(start_date_str);
            LocalDate currentDate = LocalDate.now();

            bookkeeping_days = ChronoUnit.DAYS.between(startDate, currentDate);  //计算相差的天数
        } else {
            bookkeeping_days = 0;   //无法获取则说明是第一天记账
        }
        if (bookkeeping_days != 0) {
            bookkeeping_days_text.setText(String.format("您已累计记账%d天", bookkeeping_days));
            account_num_text.setText(String.format("共计%d条流水记录", account_num));
        } else {
            bookkeeping_days_text.setText("这是您记账的第一天");
            account_num_text.setText(String.format("已产生%d条流水记录", account_num));
        }

        //创建列表视图的适配器
        runningAccountRecyclerAdapter = new RunningAccountRecyclerAdapter(runningAccountList, this);
        runningAccountRecyclerView = binding.runningAccountRecyclerView;
        runningAccountRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));  //设置线性布局
        runningAccountRecyclerView.setAdapter(runningAccountRecyclerAdapter);
    }

    /**
     * 将新建的流水添加至列表视图
     *
     * @param resultIntent 包含流水数据的意图对象
     */
    private void onNewAccountAdded(@NonNull Intent resultIntent) {
        ContentValues basic_values, special_values;           //基本数据和特殊数据记录
        SQLiteDatabase db = running_account_db_helper.openWriteLink();   //数据库写连接

        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            db.close();
            NullPointerException e = new NullPointerException("无法获取新建的流水数据");
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            return;
        }

        //获取基本流水数据
        RunningAccountType type = RunningAccountType.valueOf(resultIntent.getStringExtra(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        boolean isDefaultRemark = dataBundle.getBoolean(KeyValueStrings.ACCOUNT_IS_DEFAULT_REMARK.getValue());
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        long tag_no = dataBundle.getLong(KeyValueStrings.TAG_NO.getValue());
        basic_values = new ContentValues();
        basic_values.put(BookKeepingColumns.TYPE.toString(), type.toString());                   //种类
        basic_values.put(BookKeepingColumns.AMOUNT.toString(), amount);                          //金额
        basic_values.put(BookKeepingColumns.REMARK.toString(), isDefaultRemark ? null : remark); //备注
        basic_values.put(BookKeepingColumns.DATETIME.toString(), date_time);                     //日期
        basic_values.put(BookKeepingColumns.TAG_NO.toString(), tag_no);                          //标签编号

        long rno = db.insert(BookKeepingTables.BASIC.toString(), null, basic_values);   //获取自增主键值

        //获取特殊数据并实例化流水类
        special_values = new ContentValues();
        RunningAccountBase newRunningAccountView;
        if (type == RunningAccountType.EXPENSE) {
            newRunningAccountView = new ExpenseRunningAccount(remark, date_time, amount, isDefaultRemark);
        } else if (type == RunningAccountType.INCOME) {
            newRunningAccountView = new IncomeRunningAccount(remark, date_time, amount, isDefaultRemark);
        } else if (type == RunningAccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());    //转出账户
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());    //转入账户

            special_values.put(BookKeepingColumns.RNO.toString(), rno);
            special_values.put(BookKeepingColumns.EXPORT.toString(), exportAccount);
            special_values.put(BookKeepingColumns.IMPORT.toString(), importAccount);
            db.insert(BookKeepingTables.TRANSFER.toString(), null, special_values);

            newRunningAccountView = new TransferRunningAccount(remark, date_time, amount, isDefaultRemark, exportAccount, importAccount);
        } else {
            NullPointerException e = new NullPointerException("流水类型获取失败");
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            return;
        }

        db.close();
        newRunningAccountView.setRno(rno);  //将自增主键值保存
        runningAccountRecyclerAdapter.addNewRunningAccountView(newRunningAccountView);  //将新建的流水视图添加至列表视图适配器
        runningAccountRecyclerView.scrollToPosition(0);     //滚动到顶部（因为添加的新记录在顶部）
        Toast.makeText(getActivity(), "成功添加一条流水记录", Toast.LENGTH_SHORT).show();

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
        ContentValues basic_values, special_values;           //基本数据和特殊数据记录
        SQLiteDatabase db = running_account_db_helper.openWriteLink();   //数据库写连接

        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            NullPointerException e = new NullPointerException("无法获取修改后的流水数据");
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            return;
        }

        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        int position = dataBundle.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), -1);    //原视图下标
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        boolean isDefaultRemark = dataBundle.getBoolean(KeyValueStrings.ACCOUNT_IS_DEFAULT_REMARK.getValue());
        String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        long tag_no = dataBundle.getLong(KeyValueStrings.TAG_NO.getValue());

        //将基本数据存放至数据库
        basic_values = new ContentValues();
        basic_values.put(BookKeepingColumns.TYPE.toString(), type.toString());                   //种类
        basic_values.put(BookKeepingColumns.AMOUNT.toString(), amount);                          //金额
        basic_values.put(BookKeepingColumns.REMARK.toString(), isDefaultRemark ? null : remark); //备注
        basic_values.put(BookKeepingColumns.DATETIME.toString(), date_time);                     //日期
        basic_values.put(BookKeepingColumns.TAG_NO.toString(), tag_no);                          //标签编号
        String selection = BookKeepingColumns.RNO + "=?";
        long rno = (runningAccountRecyclerAdapter.getItem(position)).getRno();                      //编号
        String[] selectionArgs = new String[]{String.valueOf(rno)};
        db.update(
                BookKeepingTables.BASIC.toString(),
                basic_values,
                selection,
                selectionArgs
        );

        //实例化流水类
        RunningAccountBase newRunningAccountView;
        special_values = new ContentValues();
        if (type == RunningAccountType.EXPENSE) {
            newRunningAccountView = new ExpenseRunningAccount(remark, date_time, amount, isDefaultRemark);
        } else if (type == RunningAccountType.INCOME) {
            newRunningAccountView = new IncomeRunningAccount(remark, date_time, amount, isDefaultRemark);
        } else if (type == RunningAccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());    //转出账户
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());    //转入账户

            special_values.put(BookKeepingColumns.EXPORT.toString(), exportAccount);
            special_values.put(BookKeepingColumns.IMPORT.toString(), importAccount);
            db.update(
                    BookKeepingTables.TRANSFER.toString(),
                    special_values,
                    selection,
                    selectionArgs
            );

            newRunningAccountView = new TransferRunningAccount(remark, date_time, amount, isDefaultRemark, exportAccount, importAccount);
        } else {
            NullPointerException e = new NullPointerException("流水类型获取失败");
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            return;
        }

        db.close();
        newRunningAccountView.setRno(rno);
        runningAccountRecyclerAdapter.modifyRunningAccountView(position, newRunningAccountView);
        Toast.makeText(getActivity(), "成功修改流水记录", Toast.LENGTH_SHORT).show();
    }

    /**
     * 删除流水
     */
    private void onAccountDeleted(@NonNull Intent resultIntent) {
        Bundle dataBundle = resultIntent.getExtras();
        SQLiteDatabase db = running_account_db_helper.openWriteLink();

        int position;
        if (dataBundle == null) {
            NullPointerException e = new NullPointerException("无法获取流水记录下标");
            ExceptionHelper.showExceptionDialog(requireContext(), e);
            return;
        }

        //从数据库中删除
        position = dataBundle.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), -1);
        RunningAccountBase target_running_account_view = runningAccountRecyclerAdapter.getItem(position);
        long rno = target_running_account_view.getRno();
        String selection = BookKeepingColumns.RNO + "=?";
        String[] selectionArgs = {String.valueOf(rno)};
        db.delete(
                BookKeepingTables.BASIC.toString(),
                selection,
                selectionArgs
        );
        RunningAccountType type = target_running_account_view.getType();
        if (type == RunningAccountType.TRANSFER) {
            db.delete(
                    BookKeepingTables.TRANSFER.toString(),
                    selection,
                    selectionArgs
            );
        }

        db.close();
        runningAccountRecyclerAdapter.deleteRunningAccountView(position);
        Toast.makeText(getActivity(), "流水记录已删除", Toast.LENGTH_SHORT).show();

        //刷新流水记录数量文本
        account_num--;
        refreshAccountNumText();
    }

    /**
     * 从数据库中加载流水视图
     */
    @NonNull
    private List<RunningAccountBase> loadRunningAccountData() {
        SQLiteDatabase db = running_account_db_helper.openReadLink();  //获取读连接

        //定义查询光标
        Cursor basic_cursor = db.query(
                BookKeepingTables.BASIC.toString(),
                null,
                null,           //无WHERE子句
                null,
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
                    String selection = BookKeepingColumns.RNO + "=?";
                    String[] selectionArgs = {String.valueOf(rno)};

                    Cursor transfer_cursor = db.query(
                            BookKeepingTables.TRANSFER.toString(),
                            columns,
                            selection,
                            selectionArgs,
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

    /**
     * 获取开始记账的日期
     *
     * @return 开始记账的日期字符串（无法获取则为空串）
     */
    private String getBookKeepingStartDate() {
        String start_date_str = BookKeepingStartDatePreference.getStartDate(requireContext());
        if (start_date_str.isEmpty()) {
            try {
                start_date_str = RunningAccountBase.getEarliestAccountDate(requireContext());
                if (!start_date_str.isEmpty()) {
                    BookKeepingStartDatePreference.saveStartDate(start_date_str, requireContext());
                }
            } catch (SQLiteException e) {
                ExceptionHelper.showExceptionDialog(requireContext(), e);
            }
        }

        return start_date_str;
    }

    //刷新流水记录数量文本
    @SuppressLint("DefaultLocale")
    private void refreshAccountNumText() {
        if (bookkeeping_days != 0) {
            account_num_text.setText(String.format("共计%d条流水记录", account_num));
        } else {
            account_num_text.setText(String.format("已产生%d条流水记录", account_num));
        }
    }
}