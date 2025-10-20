package com.project.manager.ui.bookkeeping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.manager.R;

import java.util.List;

public class FlowRecyclerAdapter extends RecyclerView.Adapter<FlowViewHolder> {
    private List<FlowBase> flowList;   //数据源
    private Context context;

    /**
     * 构造方法
     *
     * @param flowList 流水数据类型列表
     * @param context  上下文
     */
    public FlowRecyclerAdapter(Context context, List<FlowBase> flowList) {
        this.flowList = flowList;
        this.context = context;
    }

    @NonNull
    @Override
    public FlowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.flow_view, parent, false);
        return new FlowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlowViewHolder holder, int position) {
        FlowBase currentFlow = flowList.get(position);
        String name_date = String.format("%s·%s", currentFlow.name, currentFlow.date_time);

        holder.remark_text.setText(currentFlow.remark);                 //备注
        holder.name_datetime_text.setText(name_date);                   //名称和日期
        holder.amount_text.setText(String.valueOf(currentFlow.amount)); //金额

        //设置点击事件

    }

    @Override
    public int getItemCount() {
        return this.flowList.size();
    }

    /**
     * 获取指定位置的流水数据类型
     * @param position 指定的下标
     * @return 流水数据类型
     */
    public FlowBase getItem(int position) {
        return flowList.get(position);
    }

    /**
     * 添加新流水视图
     *
     * @param newFlowView 待添加的流水视图
     */
    public void addNewFlowView(FlowBase newFlowView) {
        this.flowList.add(newFlowView);
        notifyDataSetChanged();
    }

    /**
     * 替换指定下标的流水视图
     *
     * @param position 待覆盖的视图的下标
     * @param flowView 新的流水视图
     */
    public void setFlowView(int position, FlowBase flowView) {
        this.flowList.set(position, flowView);
        notifyDataSetChanged();
    }

    /**
     * 删除指定下标的流水记录
     *
     * @param position 待删除的流水记录的下标
     */
    public void deleteFlowView(int position) {
        if (position == -1) return;

        this.flowList.remove(position);
        notifyDataSetChanged();
    }
}
