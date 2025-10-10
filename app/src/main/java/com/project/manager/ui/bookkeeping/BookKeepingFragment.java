package com.project.manager.ui.bookkeeping;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.project.manager.R;
import com.project.manager.databinding.FragmentBookkeepingBinding;
import com.project.manager.pages.newflow.NewFlowActivity;
import com.project.manager.RequestResultCode;
import com.project.manager.pages.newflow.fragments.FlowTypeEnum;

import java.util.ArrayList;
import java.util.Objects;

public class BookKeepingFragment extends Fragment implements View.OnClickListener {
    FlowListAdapter flowListAdapter;    //流水列表适配器

    private FragmentBookkeepingBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBookkeepingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //绑定单击按钮监听器
        root.findViewById(R.id.flow_btn).setOnClickListener(this);
        root.findViewById(R.id.report_btn).setOnClickListener(this);

        //创建列表视图的适配器
        flowListAdapter = new FlowListAdapter(requireActivity(), new ArrayList<>());
        ListView flowListView = binding.flowList;
        flowListView.setAdapter(flowListAdapter);

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestResultCode.NEW_FLOW_REQUEST.ordinal() && resultCode == RequestResultCode.RESULT_OK.ordinal()) {
            //获取基本流水数据
            FlowTypeEnum type = FlowTypeEnum.valueOf(data.getStringExtra("type"));                  //种类
            String name = data.getStringExtra("name");                                              //名称
            String remark = data.getStringExtra("remark");                                          //备注
            String date = data.getStringExtra("date");                                              //日期
            int amount = Integer.parseInt(Objects.requireNonNull(data.getStringExtra("amount")));   //金额

            //实例化流水类
            FlowViewBase newFlowView = null;
            if (type == FlowTypeEnum.EXPENSE) {
                newFlowView = new ExpenseFlowView(type, name, remark, date, amount);
            } else if (type == FlowTypeEnum.INCOME) {
                newFlowView = new IncomeFlowView(type, name, remark, date, amount);
            } else if (type == FlowTypeEnum.TRANSFER) {
                String exportAccount = data.getStringExtra("exportAccount");    //转出账户
                String importAccount = data.getStringExtra("importAccount");    //转入账户
                newFlowView = new TransferFlowView(type, name, remark, date, amount, exportAccount, importAccount);
            }

            //将新建的流水视图添加至列表视图适配器
            if (newFlowView != null) {
                flowListAdapter.addNewFlow(newFlowView);
            }
        }
    }
}