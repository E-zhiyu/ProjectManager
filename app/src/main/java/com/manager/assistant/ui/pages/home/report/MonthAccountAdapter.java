package com.manager.assistant.ui.pages.home.report;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.data.data_class.MonthAccountInfo;

import java.util.ArrayList;
import java.util.List;

public class MonthAccountAdapter extends RecyclerView.Adapter<MonthAccountAdapter.MonthAccountViewHolder> {
    private final List<MonthAccountInfo> monthAccountInfoList = new ArrayList<>();  //每月流水数据列表
    private ReportActivity.MonthAccountInfoType monthAccountInfoType;               //显示的每月流水数据的种类
    private final Context context;                                                  //上下文

    public static class MonthAccountViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView monthNameText;         //月份文本
        MaterialTextView proportionText;        //金额占比文本
        MaterialTextView amountText;            //金额文本
        LinearProgressIndicator proportionBar;  //占比进度条

        public MonthAccountViewHolder(@NonNull View itemView) {
            super(itemView);

            monthNameText = itemView.findViewById(R.id.source_name_text);
            proportionText = itemView.findViewById(R.id.percentage_text);
            amountText = itemView.findViewById(R.id.amount_text);
            proportionBar = itemView.findViewById(R.id.percentage_bar);
        }
    }

    /**
     * 每月流水信息显示适配器
     *
     * @param monthAccountInfoType 显示的流水信息种类
     * @param context              上下文
     */
    public MonthAccountAdapter(ReportActivity.MonthAccountInfoType monthAccountInfoType, Context context) {
        this.monthAccountInfoType = monthAccountInfoType;
        this.context = context;
    }

    @NonNull
    @Override
    public MonthAccountAdapter.MonthAccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_amount_proportion, parent, false);
        return new MonthAccountAdapter.MonthAccountViewHolder(view);
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void onBindViewHolder(@NonNull MonthAccountAdapter.MonthAccountViewHolder holder, int position) {
        MonthAccountInfo oneMonthInfo = monthAccountInfoList.get(position);
        double amount = 0;
        switch (monthAccountInfoType) {
            case BALANCE:
                amount = oneMonthInfo.getIncome() - oneMonthInfo.getExpense();

                if (amount < 0) {   //如果结余为负数，则将进度条设置为红色
                    holder.proportionBar.setIndicatorColor(ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_error));
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
        String month_name = String.format("%d月", position + 1);

        holder.monthNameText.setText(month_name);                 //月份名称
        holder.amountText.setText(String.format("%.2f", amount));  //金额
        String percentage_str = String.format("%d%%", percentage);
        holder.proportionText.setText(percentage_str);             //百分比文本
        holder.proportionBar.setProgress(percentage);              //百分比进度条
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

        int old_item_count = getItemCount();
        this.monthAccountInfoList.clear();
        notifyItemRangeRemoved(0, old_item_count);

        this.monthAccountInfoList.addAll(monthAccountInfoList);
        notifyItemRangeInserted(0, monthAccountInfoList.size());
    }
}
