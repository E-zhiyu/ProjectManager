package com.manager.assistant.ui.pages.report;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.R;
import com.manager.assistant.data.classes.MonthAccountInfo;
import com.manager.assistant.databinding.ViewHolderAmountProportionBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MonthAccountAdapter extends RecyclerView.Adapter<MonthAccountAdapter.MonthAccountViewHolder> {
    private final List<MonthAccountInfo> monthAccountInfoList = new ArrayList<>();  //每月流水数据列表
    private ReportActivity.MonthAccountInfoType monthAccountInfoType;               //显示的每月流水数据的种类

    public static class MonthAccountViewHolder extends RecyclerView.ViewHolder {
        ViewHolderAmountProportionBinding binding;

        public MonthAccountViewHolder(@NonNull ViewHolderAmountProportionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /**
     * 每月流水信息显示适配器
     *
     * @param monthAccountInfoType 显示的流水信息种类
     */
    public MonthAccountAdapter(ReportActivity.MonthAccountInfoType monthAccountInfoType) {
        this.monthAccountInfoType = monthAccountInfoType;
    }

    @NonNull
    @Override
    public MonthAccountAdapter.MonthAccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderAmountProportionBinding binding = ViewHolderAmountProportionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MonthAccountAdapter.MonthAccountViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthAccountAdapter.MonthAccountViewHolder holder, int position) {
        MonthAccountInfo oneMonthInfo = monthAccountInfoList.get(position);
        double amount = 0;
        switch (monthAccountInfoType) {
            case BALANCE:
                amount = oneMonthInfo.getIncome() - oneMonthInfo.getExpense();

                if (amount < 0) {   //如果结余为负数，则将进度条设置为红色
                    holder.binding.percentageBar.setIndicatorColor(
                            holder.itemView.getContext().getColor(R.color.md_theme_error)
                    );
                }
                break;
            case EXPENSE:
                amount = oneMonthInfo.getExpense();
                break;
            case INCOME:
                amount = oneMonthInfo.getIncome();
                break;
        }
        int percentage = oneMonthInfo.getPercentage();
        String monthName = String.format(Locale.getDefault(), "%d月", position + 1);

        holder.binding.sourceNameText.setText(monthName);                   //月份名称
        holder.binding.amountText.setText(String.format(Locale.getDefault(), "%.2f", amount));  //金额
        String percentageStr = String.format(Locale.getDefault(), "%d%%", percentage);
        holder.binding.percentageText.setText(percentageStr);               //百分比文本
        holder.binding.percentageBar.setProgress(percentage);              //百分比进度条
    }

    @Override
    public int getItemCount() {
        return monthAccountInfoList.size();
    }

    /**
     * 刷新每月流水记录
     *
     * @param monthAccountInfoList 刷新后的流水数据列表
     * @param type                 刷新后的流水显示类型
     */
    public void refreshMonthAccountInfo(List<MonthAccountInfo> monthAccountInfoList, ReportActivity.MonthAccountInfoType type) {
        this.monthAccountInfoType = type;

        int oldItemCount = getItemCount();
        this.monthAccountInfoList.clear();
        notifyItemRangeRemoved(0, oldItemCount);

        this.monthAccountInfoList.addAll(monthAccountInfoList);
        notifyItemRangeInserted(0, monthAccountInfoList.size());
    }
}
