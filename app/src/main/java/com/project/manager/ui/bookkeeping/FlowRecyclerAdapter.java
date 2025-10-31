package com.project.manager.ui.bookkeeping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;

import java.util.List;

public class FlowRecyclerAdapter extends RecyclerView.Adapter<FlowRecyclerAdapter.FlowViewHolder> {
    private final List<FlowBase> flowList;   //数据源
    private final OnFlowClickListener listener;   //单击接口对象

    /**
     * 构造方法
     *
     * @param flowList 流水数据类型列表
     */
    public FlowRecyclerAdapter(List<FlowBase> flowList, OnFlowClickListener listener) {
        this.flowList = flowList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.flow_view, parent, false);
        return new FlowViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FlowViewHolder holder, int position) {
        FlowBase currentFlow = flowList.get(position);
        String name_and_datetime = String.format("%s·%s", currentFlow.name, currentFlow.date_time);

        holder.remark_text.setText(currentFlow.remark);                 //备注
        holder.name_datetime_text.setText(name_and_datetime);           //名称和日期
        holder.amount_text.setText(String.valueOf(currentFlow.amount)); //金额
    }

    @Override
    public int getItemCount() {
        return this.flowList.size();
    }

    // 定义点击事件接口
    public interface OnFlowClickListener {
        void onFlowClick(int position, FlowBase flowBase);
    }

    public class FlowViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView amount_text, remark_text, name_datetime_text;

        public FlowViewHolder(@NonNull View itemView, OnFlowClickListener listener) {
            super(itemView);
            amount_text = itemView.findViewById(R.id.amount_textview);
            remark_text = itemView.findViewById(R.id.remark_textview);
            name_datetime_text = itemView.findViewById(R.id.name_datetime_textview);

            //绑定点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onFlowClick(getAdapterPosition(), flowList.get(getAdapterPosition()));
                }
            });
        }
    }

    /**
     * 获取指定位置的流水数据类型
     *
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
        this.flowList.add(0, newFlowView);
        notifyItemInserted(0);
    }

    /**
     * 替换指定下标的流水视图
     *
     * @param position 待覆盖的视图的下标
     * @param flowView 新的流水视图
     */
    public void setFlowView(int position, FlowBase flowView) {
        this.flowList.set(position, flowView);
        notifyItemChanged(position);
    }

    /**
     * 删除指定下标的流水记录
     *
     * @param position 待删除的流水记录的下标
     */
    public void deleteFlowView(int position) {
        if (position == -1) return;

        this.flowList.remove(position);
        notifyItemRemoved(position);
    }
}
