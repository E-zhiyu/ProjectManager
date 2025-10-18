package com.project.manager.ui.bookkeeping;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.project.manager.R;
import com.project.manager.database.FlowDatabaseHelper;
import com.project.manager.databinding.FragmentBookkeepingBinding;
import com.project.manager.ui.bookkeeping.flow_edit.FlowEditActivity;
import com.project.manager.ui.bookkeeping.new_flow.NewFlowActivity;
import com.project.manager.RequestResultCode;
import com.project.manager.ui.bookkeeping.new_flow.new_flow_fragments.FlowTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookKeepingFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    FlowListAdapter flowListAdapter;    //流水列表适配器
    FlowDatabaseHelper flow_db_helper;       //流水数据库帮助器

    private FragmentBookkeepingBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBookkeepingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //实例化数据库帮助器
        flow_db_helper = new FlowDatabaseHelper(getActivity());

        //绑定单击按钮监听器
        root.findViewById(R.id.flow_btn).setOnClickListener(this);
        root.findViewById(R.id.report_btn).setOnClickListener(this);

        //创建列表视图的适配器
        List<FlowViewBase> flowList = new ArrayList<>();
        flowListAdapter = new FlowListAdapter(requireActivity(), flowList);
        ListView flowListView = binding.flowList;
        flowListView.setAdapter(flowListAdapter);
        flowListView.setOnItemClickListener(this);  //设置单击监听器

        loadFlowViews();  //从数据库加载流水视图

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.flow_btn) {  //新建流水
            Intent intent = new Intent(getActivity(), NewFlowActivity.class);
            startActivityForResult(intent, RequestResultCode.NEW_FLOW_REQUEST.ordinal());
        } else if (v.getId() == R.id.report_btn) {  //查看报表

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);

        if (resultCode == RequestResultCode.RESULT_REJECT.ordinal() || resultCode == 0) {
            return;
        } else if (resultCode == RequestResultCode.RESULT_DELETE_FLOW.ordinal()) {  //删除
            deleteFlow(resultIntent);
        } else if (requestCode == RequestResultCode.NEW_FLOW_REQUEST.ordinal()) {   //添加
            addNewFlow(resultIntent);
        } else if (requestCode == RequestResultCode.EDIT_FLOW_REQUEST.ordinal()) {  //修改
            coverFlowAfterEditing(resultIntent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FlowListAdapter flowListAdapter = (FlowListAdapter) parent.getAdapter();
        FlowViewBase flowView = (FlowViewBase) flowListAdapter.getItem(position);

        Intent skip2FlowEdit = new Intent(getActivity(), FlowEditActivity.class);
        Bundle dataBundle = new Bundle();

        //获取基本数据
        FlowTypeEnum type = flowView.type;      //类型
        dataBundle.putString(FlowAttributeStrings.TYPE, type.toString());
        double amount = flowView.amount;        //金额
        dataBundle.putDouble(FlowAttributeStrings.AMOUNT, amount);
        String remark = flowView.remark;        //备注
        dataBundle.putString(FlowAttributeStrings.REMARK, remark);
        String date_time = flowView.date_time;  //日期
        dataBundle.putString(FlowAttributeStrings.DATETIME, date_time);
        long fno = flowView.fno;                //编号
        dataBundle.putLong(FlowAttributeStrings.FNO, fno);

        dataBundle.putInt(FlowAttributeStrings.POSITION, position);  //将待修改的流水实例下标放入包裹

        //获取特殊数据
        if (type == FlowTypeEnum.TRANSFER) {
            String exportAccount = ((TransferFlowView) flowView).exportAccount;  //转出账户
            dataBundle.putString(FlowAttributeStrings.EXPORT, exportAccount);
            String importAccount = ((TransferFlowView) flowView).importAccount;  //转入账户
            dataBundle.putString(FlowAttributeStrings.IMPORT, importAccount);
        }

        skip2FlowEdit.putExtras(dataBundle);
        startActivityForResult(skip2FlowEdit, RequestResultCode.EDIT_FLOW_REQUEST.ordinal());
    }

    /**
     * 将新建的流水添加至列表视图
     *
     * @param resultIntent 包含流水数据的意图对象
     */
    private void addNewFlow(Intent resultIntent) {
        ContentValues basic_values, special_values;           //基本数据和特殊数据记录
        SQLiteDatabase db = flow_db_helper.openWriteLink();   //数据库写连接

        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            db.close();
            throw new NullPointerException("获取新建流水数据时出错");
        }

        //获取基本流水数据
        FlowTypeEnum type = FlowTypeEnum.valueOf(resultIntent.getStringExtra(FlowAttributeStrings.TYPE));
        String remark, date_time;
        double amount;
        remark = dataBundle.getString(FlowAttributeStrings.REMARK);
        amount = dataBundle.getDouble(FlowAttributeStrings.AMOUNT, -1);
        date_time = dataBundle.getString(FlowAttributeStrings.DATETIME);
        basic_values = new ContentValues();
        basic_values.put(FlowDatabaseHelper.COLUMN_TYPE, type.toString());   //种类
        basic_values.put(FlowDatabaseHelper.COLUMN_AMOUNT, amount);          //金额
        basic_values.put(FlowDatabaseHelper.COLUMN_REMARK, remark);          //备注
        basic_values.put(FlowDatabaseHelper.COLUMN_DATETIME, date_time);     //日期
        long fno = db.insert(FlowDatabaseHelper.TABLE_BASIC, null, basic_values);   //获取自增主键值

        //获取特殊数据并实例化流水类
        special_values = new ContentValues();
        FlowViewBase newFlowView;
        if (type == FlowTypeEnum.EXPENSE) {
            newFlowView = new ExpenseFlowView(remark, date_time, amount);
        } else if (type == FlowTypeEnum.INCOME) {
            newFlowView = new IncomeFlowView(remark, date_time, amount);
        } else if (type == FlowTypeEnum.TRANSFER) {
            String exportAccount = dataBundle.getString(FlowAttributeStrings.EXPORT);    //转出账户
            String importAccount = dataBundle.getString(FlowAttributeStrings.IMPORT);    //转入账户

            special_values.put(FlowDatabaseHelper.COLUMN_FNO, fno);
            special_values.put(FlowDatabaseHelper.COLUMN_EXPORT, exportAccount);
            special_values.put(FlowDatabaseHelper.COLUMN_IMPORT, importAccount);
            db.insert(FlowDatabaseHelper.TABLE_TRANSFER, null, special_values);

            newFlowView = new TransferFlowView(remark, date_time, amount, exportAccount, importAccount);
        } else {
            throw new NullPointerException("流水类型获取失败");
        }

        db.close();
        newFlowView.fno = fno;  //将自增主键值保存
        flowListAdapter.addNewFlowView(newFlowView);  //将新建的流水视图添加至列表视图适配器
        Toast.makeText(getActivity(), "成功添加一条流水记录", Toast.LENGTH_SHORT).show();
    }

    /**
     * 用编辑后的流水数据覆盖原来的流水
     *
     * @param resultIntent 带有编辑后数据的意图对象
     */
    private void coverFlowAfterEditing(Intent resultIntent) {
        ContentValues basic_values, special_values;           //基本数据和特殊数据记录
        SQLiteDatabase db = flow_db_helper.openWriteLink();   //数据库写连接

        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            throw new NullPointerException("读取编辑后的流水数据时出错");
        }

        FlowTypeEnum type = FlowTypeEnum.valueOf(dataBundle.getString("type"));
        int position = dataBundle.getInt(FlowAttributeStrings.POSITION, -1);    //原视图下标
        double amount = dataBundle.getDouble(FlowAttributeStrings.AMOUNT, -1);
        String remark = dataBundle.getString(FlowAttributeStrings.REMARK);
        String date_time = dataBundle.getString(FlowAttributeStrings.DATETIME);

        //将基本数据存放至数据库
        basic_values = new ContentValues();
        basic_values.put(FlowDatabaseHelper.COLUMN_TYPE, type.toString());  //种类
        basic_values.put(FlowDatabaseHelper.COLUMN_AMOUNT, amount);         //金额
        basic_values.put(FlowDatabaseHelper.COLUMN_REMARK, remark);         //备注
        basic_values.put(FlowDatabaseHelper.COLUMN_DATETIME, date_time);    //日期
        String selection = FlowAttributeStrings.FNO + "=?";
        long fno = ((FlowViewBase) flowListAdapter.getItem(position)).fno;  //编号
        String[] selectionArgs = new String[]{String.valueOf(fno)};
        db.update(
                FlowDatabaseHelper.TABLE_BASIC,
                basic_values,
                selection,
                selectionArgs
        );

        //实例化流水类
        FlowViewBase newFlowView;
        special_values = new ContentValues();
        if (type == FlowTypeEnum.EXPENSE) {
            newFlowView = new ExpenseFlowView(remark, date_time, amount);
        } else if (type == FlowTypeEnum.INCOME) {
            newFlowView = new IncomeFlowView(remark, date_time, amount);
        } else if (type == FlowTypeEnum.TRANSFER) {
            String exportAccount = dataBundle.getString(FlowAttributeStrings.EXPORT);    //转出账户
            String importAccount = dataBundle.getString(FlowAttributeStrings.IMPORT);    //转入账户

            special_values.put(FlowDatabaseHelper.COLUMN_EXPORT, exportAccount);
            special_values.put(FlowDatabaseHelper.COLUMN_IMPORT, importAccount);
            db.update(
                    FlowDatabaseHelper.TABLE_TRANSFER,
                    special_values,
                    selection,
                    selectionArgs
            );

            newFlowView = new TransferFlowView(remark, date_time, amount, exportAccount, importAccount);
        } else {
            throw new NullPointerException("流水类型获取失败");
        }

        newFlowView.fno = fno;
        flowListAdapter.setFlowView(position, newFlowView);
        Toast.makeText(getActivity(), "成功修改流水记录", Toast.LENGTH_SHORT).show();
    }

    /**
     * 删除流水
     */
    private void deleteFlow(Intent resultIntent) {
        Bundle dataBundle = resultIntent.getExtras();
        SQLiteDatabase db = flow_db_helper.openWriteLink();

        int position;
        if (dataBundle == null) {
            throw new NullPointerException("无法获取有效的流水视图下标");
        }

        //从数据库中删除
        position = dataBundle.getInt(FlowAttributeStrings.POSITION, -1);
        FlowViewBase target_flow_view = (FlowViewBase) flowListAdapter.getItem(position);
        long fno = target_flow_view.fno;
        String selection = FlowAttributeStrings.FNO + "=?";
        String[] selectionArgs = {String.valueOf(fno)};
        db.delete(
                FlowDatabaseHelper.TABLE_BASIC,
                selection,
                selectionArgs
        );
        FlowTypeEnum type = target_flow_view.type;
        if (type == FlowTypeEnum.TRANSFER) {
            db.delete(
                    FlowDatabaseHelper.TABLE_TRANSFER,
                    selection,
                    selectionArgs
            );
        }

        flowListAdapter.deleteFlowView(position);
        Toast.makeText(getActivity(), "流水记录已删除", Toast.LENGTH_SHORT).show();
    }

    /**
     * 从数据库中加载流水视图
     */
    private void loadFlowViews() {
        SQLiteDatabase db = flow_db_helper.openReadLink();  //获取读连接

        //定义查询光标
        Cursor basic_cursor = db.query(
                FlowDatabaseHelper.TABLE_BASIC,
                null,   //查询所有列
                null,           //无WHERE子句
                null,
                null,
                null,
                FlowDatabaseHelper.COLUMN_DATETIME
        );

        //查询数据
        List<FlowViewBase> flowViewList = new ArrayList<>();
        while (basic_cursor.moveToNext()) {
            long fno = basic_cursor.getInt(basic_cursor.getColumnIndexOrThrow(FlowDatabaseHelper.COLUMN_FNO));
            double amount = basic_cursor.getDouble(basic_cursor.getColumnIndexOrThrow(FlowDatabaseHelper.COLUMN_AMOUNT));
            FlowTypeEnum type = FlowTypeEnum.valueOf(basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(FlowDatabaseHelper.COLUMN_TYPE)));
            String remark = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(FlowDatabaseHelper.COLUMN_REMARK));
            String date_time = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(FlowDatabaseHelper.COLUMN_DATETIME));

            FlowViewBase flowView = null;
            switch (type) {
                case EXPENSE:
                    flowView = new ExpenseFlowView(fno, remark, date_time, amount);
                    break;
                case INCOME:
                    flowView = new IncomeFlowView(fno, remark, date_time, amount);
                    break;
                case TRANSFER:
                    String[] columns = {FlowDatabaseHelper.COLUMN_EXPORT, FlowDatabaseHelper.COLUMN_IMPORT};
                    String selection = FlowDatabaseHelper.COLUMN_FNO + "=?";
                    String[] selectionArgs = {String.valueOf(fno)};

                    Cursor transfer_cursor = db.query(
                            FlowDatabaseHelper.TABLE_TRANSFER,
                            columns,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            null
                    );

                    while (transfer_cursor.moveToNext()) {
                        String exportAccount = transfer_cursor.getString(transfer_cursor.getColumnIndexOrThrow(FlowDatabaseHelper.COLUMN_EXPORT));
                        String importAccount = transfer_cursor.getString(transfer_cursor.getColumnIndexOrThrow(FlowDatabaseHelper.COLUMN_IMPORT));
                        transfer_cursor.close();
                        flowView = new TransferFlowView(fno, remark, date_time, amount, exportAccount, importAccount);
                    }

                    break;
                default:
                    throw new RuntimeException("无法获取正确的流水视图类型");
            }
            flowViewList.add(flowView);
        }
        basic_cursor.close();
        db.close();

        flowListAdapter.initFlowView(flowViewList);  //初始化列表视图
    }
}