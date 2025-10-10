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

public abstract class FlowFragmentBase extends Fragment {
    FragmentBookkeepingBinding binding;     //父界面索引
    View xmlView;                           //绑定的XML界面
    protected String name;           //碎片名称
    protected FlowTypeEnum type;     //流水类型

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookkeepingBinding.inflate(inflater, container, false);

        xmlView = inflater.inflate(getLayoutResId(), container, false);
        initViews(xmlView);
        return xmlView;
    }

    public String getName() {
        return name;
    }

    public FlowTypeEnum getType() {
        return type;
    }

    protected abstract int getLayoutResId();

    //初始化碎片布局
    protected abstract void initViews(View view);

    /**
     * 获取流水日期
     *
     * @return 流水日期字符串
     */
    public String getDate() {
        TextView dateTextView = xmlView.findViewById(R.id.flow_date_textview);
        return dateTextView.getText().toString();
    }

    /**
     * 获取流水备注
     *
     * @return 流水备注字符串
     */
    public String getRemark() {
        EditText remarkEditText = xmlView.findViewById(R.id.flow_remark_edittext);
        return remarkEditText.getText().toString();
    }

    /**
     * 获取流水金额
     *
     * @return 流水金额
     */
    public int getAmount() {
        EditText remarkEditText = xmlView.findViewById(R.id.et_amount);
        return Integer.parseInt(remarkEditText.getText().toString());
    }
}

