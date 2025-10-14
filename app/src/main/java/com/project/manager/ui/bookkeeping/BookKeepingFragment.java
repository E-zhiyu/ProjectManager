package com.project.manager.ui.bookkeeping;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.project.manager.R;
import com.project.manager.databinding.FragmentBookkeepingBinding;
import com.project.manager.ui.bookkeeping.flow_edit.FlowEditActivity;
import com.project.manager.ui.bookkeeping.new_flow.NewFlowActivity;
import com.project.manager.RequestResultCode;
import com.project.manager.ui.bookkeeping.flow_type.FlowTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BookKeepingFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    FlowListAdapter flowListAdapter;    //流水列表适配器

    private FragmentBookkeepingBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBookkeepingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //绑定单击按钮监听器
        root.findViewById(R.id.flow_btn).setOnClickListener(this);
        root.findViewById(R.id.report_btn).setOnClickListener(this);

        List<FlowViewBase> flowList = new ArrayList<>();
        //仅供调试
        ExpenseFlowView testExpense = new ExpenseFlowView("测试项", "2025年10月12日", 10);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);
        flowList.add(testExpense);

        //创建列表视图的适配器
        flowListAdapter = new FlowListAdapter(requireActivity(), flowList);
        ListView flowListView = binding.flowList;
        flowListView.setAdapter(flowListAdapter);
        flowListView.setOnItemClickListener(this);  //设置单击监听器

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

        if (resultCode == RequestResultCode.RESULT_DELETE_FLOW.ordinal()) {
            int position = Objects.requireNonNull(resultIntent.getExtras()).getInt("position", -1);
            flowListAdapter.deleteFlowView(position);
        } else if (requestCode == RequestResultCode.NEW_FLOW_REQUEST.ordinal()) {
            addNewFlow(resultIntent);
        } else if (requestCode == RequestResultCode.EDIT_FLOW_REQUEST.ordinal()) {
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
        FlowTypeEnum type = flowView.type;  //类型
        dataBundle.putString("type", type.toString());
        double amount = flowView.amount;       //金额
        dataBundle.putDouble("amount", amount);
        String remark = flowView.remark;    //备注
        dataBundle.putString("remark", remark);
        String date = flowView.date;        //日期
        dataBundle.putString("date", date);

        dataBundle.putInt("position", position);  //将待修改的流水实例下标放入包裹

        //获取特殊数据
        if (type == FlowTypeEnum.TRANSFER) {
            String exportAccount = ((TransferFlowView) flowView).exportAccount;  //转出账户
            dataBundle.putString("exportAccount", exportAccount);
            String importAccount = ((TransferFlowView) flowView).importAccount;  //转入账户
            dataBundle.putString("importAccount", importAccount);
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
        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            throw new NullPointerException("获取新建流水数据时出错");
        }

        //获取基本流水数据
        FlowTypeEnum type = FlowTypeEnum.valueOf(resultIntent.getStringExtra("type"));          //种类
        String remark, date;                                               //日期
        double amount;    //金额
        remark = dataBundle.getString("remark");
        amount = dataBundle.getDouble("amount", -1);
        date = dataBundle.getString("date");

        //实例化流水类
        FlowViewBase newFlowView;
        if (type == FlowTypeEnum.EXPENSE) {
            newFlowView = new ExpenseFlowView(remark, date, amount);
        } else if (type == FlowTypeEnum.INCOME) {
            newFlowView = new IncomeFlowView(remark, date, amount);
        } else if (type == FlowTypeEnum.TRANSFER) {
            String exportAccount = dataBundle.getString("exportAccount");    //转出账户
            String importAccount = dataBundle.getString("importAccount");    //转入账户
            newFlowView = new TransferFlowView(remark, date, amount, exportAccount, importAccount);
        } else {
            throw new NullPointerException("流水类型获取失败");
        }

        //将新建的流水视图添加至列表视图适配器
        flowListAdapter.addNewFlowView(newFlowView);
    }

    /**
     * 用编辑后的流水数据覆盖原来的流水
     *
     * @param resultIntent 带有编辑后数据的意图对象
     */
    private void coverFlowAfterEditing(Intent resultIntent) {
        Bundle dataBundle = resultIntent.getExtras();
        if (dataBundle == null) {
            throw new NullPointerException("读取编辑后的流水数据时出错");
        }

        FlowTypeEnum type = FlowTypeEnum.valueOf(dataBundle.getString("type"));
        int position = dataBundle.getInt("position", -1);
        double amount = dataBundle.getDouble("amount", -1);
        String remark = dataBundle.getString("remark");
        String date = dataBundle.getString("date");

        //实例化流水类
        FlowViewBase newFlowView;
        if (type == FlowTypeEnum.EXPENSE) {
            newFlowView = new ExpenseFlowView(remark, date, amount);
        } else if (type == FlowTypeEnum.INCOME) {
            newFlowView = new IncomeFlowView(remark, date, amount);
        } else if (type == FlowTypeEnum.TRANSFER) {
            String exportAccount = dataBundle.getString("exportAccount");    //转出账户
            String importAccount = dataBundle.getString("importAccount");    //转入账户
            newFlowView = new TransferFlowView(remark, date, amount, exportAccount, importAccount);
        } else {
            throw new NullPointerException("流水类型获取失败");
        }

        flowListAdapter.setFlowView(position, newFlowView);
    }
}