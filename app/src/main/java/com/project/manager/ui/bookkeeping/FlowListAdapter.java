package com.project.manager.ui.bookkeeping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.project.manager.R;

import java.util.List;

public class FlowListAdapter extends BaseAdapter {
    private final Context context;
    private List<FlowViewBase> flowViewList;    //流水视图列表

    /**
     * 流水列表适配器构造方法
     *
     * @param context      上下文
     * @param flowViewList 流水视图列表
     */
    public FlowListAdapter(Context context, List<FlowViewBase> flowViewList) {
        this.context = context;
        this.flowViewList = flowViewList;
    }

    @Override
    public int getCount() {
        return flowViewList.size();
    }

    @Override
    public Object getItem(int position) {
        return flowViewList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //请创建自定义的流水布局xml文件
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.flow_view, parent, false);
        }

        //初始化控件
        FlowViewBase oneFlowView = flowViewList.get(position);
        TextView remarkTextView = convertView.findViewById(R.id.remark_textview);       //备注
        remarkTextView.setText(oneFlowView.remark);
        TextView nameDateTextView = convertView.findViewById(R.id.name_date_textview);  //名称和日期
        String name_date = String.format("%s·%s", oneFlowView.name, oneFlowView.date);
        nameDateTextView.setText(name_date);
        TextView amountTextView = convertView.findViewById(R.id.amount_textview);       //金额
        amountTextView.setText(String.valueOf(oneFlowView.amount));

        return convertView;
    }

    /**
     * 添加新流水视图
     *
     * @param newFlowView 待添加的流水视图
     */
    public void addNewFlowView(FlowViewBase newFlowView) {
        this.flowViewList.add(newFlowView);
        notifyDataSetChanged();
    }

    /**
     * 替换指定下标的流水视图
     *
     * @param position 待覆盖的视图的下标
     * @param flowView 新的流水视图
     */
    public void setFlowView(int position, FlowViewBase flowView) {
        this.flowViewList.set(position, flowView);
        notifyDataSetChanged();
    }

    /**
     * 删除指定下标的流水记录
     *
     * @param position 待删除的流水记录的下标
     */
    public void deleteFlowView(int position) {
        if (position == -1) return;

        this.flowViewList.remove(position);
        notifyDataSetChanged();
    }
}
