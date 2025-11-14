package com.project.manager.ui.bookkeeping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;

import java.util.List;

public class RunningAccountRecyclerAdapter extends RecyclerView.Adapter<RunningAccountRecyclerAdapter.RunningAccountViewHolder> {
    private final List<RunningAccountBase> runningAccountList;   //数据源
    private final OnRunningAccountViewClickListener listener;   //单击接口对象

    /**
     * 构造方法
     *
     * @param runningAccountList 流水数据类型列表
     */
    public RunningAccountRecyclerAdapter(List<RunningAccountBase> runningAccountList, OnRunningAccountViewClickListener listener) {
        this.runningAccountList = runningAccountList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RunningAccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.running_account_view, parent, false);
        return new RunningAccountViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RunningAccountViewHolder holder, int position) {
        RunningAccountBase currentRunningAccount = runningAccountList.get(position);
        String name_and_datetime = String.format("%s·%s",
                currentRunningAccount.getName(),
                currentRunningAccount.getDate_time()
        );

        holder.remark_text.setText(currentRunningAccount.getRemark());                  //备注
        holder.name_datetime_text.setText(name_and_datetime);                           //名称和日期
        holder.amount_text.setText(String.valueOf(currentRunningAccount.getAmount()));  //金额
    }

    @Override
    public int getItemCount() {
        return this.runningAccountList.size();
    }

    //定义流水视图点击事件接口
    public interface OnRunningAccountViewClickListener {
        void onRunningAccountViewClick(int position, RunningAccountBase runningAccountBase);
    }

    public class RunningAccountViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView amount_text, remark_text, name_datetime_text;

        public RunningAccountViewHolder(@NonNull View itemView, OnRunningAccountViewClickListener listener) {
            super(itemView);
            amount_text = itemView.findViewById(R.id.amount_text);
            remark_text = itemView.findViewById(R.id.remark_textview);
            name_datetime_text = itemView.findViewById(R.id.name_datetime_textview);

            //绑定点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onRunningAccountViewClick(getAdapterPosition(), runningAccountList.get(getAdapterPosition()));
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
    public RunningAccountBase getItem(int position) {
        return runningAccountList.get(position);
    }

    /**
     * 添加新流水视图
     *
     * @param newRunningAccountView 待添加的流水视图
     */
    public void addNewRunningAccountView(RunningAccountBase newRunningAccountView) {
        this.runningAccountList.add(0, newRunningAccountView);
        notifyItemInserted(0);
    }

    /**
     * 修改指定下标的流水视图
     *
     * @param position           待修改的视图的下标
     * @param runningAccountView 新的流水视图
     */
    public void modifyRunningAccountView(int position, RunningAccountBase runningAccountView) {
        this.runningAccountList.set(position, runningAccountView);
        notifyItemChanged(position);
    }

    /**
     * 删除指定下标的流水记录
     *
     * @param position 待删除的流水记录的下标
     */
    public void deleteRunningAccountView(int position) {
        if (position == -1) return;

        this.runningAccountList.remove(position);
        notifyItemRemoved(position);
    }
}
