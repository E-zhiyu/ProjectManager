package com.project.manager.ui.bookkeeping.flow_type;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.project.manager.R;
import com.project.manager.databinding.FragmentBookkeepingBinding;

import java.util.Objects;

public abstract class FlowFragmentBase extends Fragment {
    Bundle initData = null;                 //初始化控件内容的数据（用于编辑流水记录时）
    FragmentBookkeepingBinding binding;     //父界面索引
    View xmlView;                           //绑定的XML界面
    protected String name;                  //碎片名称
    protected FlowTypeEnum type;            //流水类型

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookkeepingBinding.inflate(inflater, container, false);

        xmlView = inflater.inflate(getLayoutResId(), container, false);
        initViews(xmlView);

        //判断是否传递了外部数据，如果传递了则将数据填入对应控件
        if (initData != null) {
            initViewsWhenEditing(initData);
        }

        return xmlView;
    }

    public String getName() {
        return name;
    }

    public FlowTypeEnum getType() {
        return type;
    }

    public void setInitData(Bundle initData) {
        this.initData = initData;
    }

    protected abstract int getLayoutResId();

    //初始化碎片布局
    protected abstract void initViews(View view);

    /**
     * 编辑流水时初始化控件内容的方法
     *
     * @param dataBundle 包含初始信息的包裹
     */
    public void initViewsWhenEditing(Bundle dataBundle) {
        EditText amountView, remarkView;
        double amount = dataBundle.getDouble("amount", -1);
        String remark = dataBundle.getString("remark");
        String date = dataBundle.getString("date");

        amountView = xmlView.findViewById(R.id.amount_textedit);        //金额
        amountView.setText(String.valueOf(amount));
        remarkView = xmlView.findViewById(R.id.remark_edittext);        //备注
        remarkView.setText(remark);
        TextView dateView = xmlView.findViewById(R.id.date_textview);   //日期
        dateView.setText(date);
    }

    /**
     * 获取流水日期
     *
     * @return 流水日期字符串
     */
    public String getDate() {
        TextView dateTextView = xmlView.findViewById(R.id.date_textview);
        return dateTextView.getText().toString();
    }

    /**
     * 获取流水备注
     *
     * @return 流水备注字符串
     */
    public String getRemark() {
        EditText remarkEditText = xmlView.findViewById(R.id.remark_edittext);
        return remarkEditText.getText().toString();
    }

    /**
     * 获取流水金额
     *
     * @return 流水金额
     */
    public double getAmount() {
        EditText remarkEditText = xmlView.findViewById(R.id.amount_textedit);
        return Double.parseDouble(remarkEditText.getText().toString());
    }
}

