package com.project.manager.ui.bookkeeping.report;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.data.data_class.MonthAccountInfo;
import com.project.manager.helpers.ColorHelper;

import java.util.List;

public class MonthAccountAdapter extends RecyclerView.Adapter<MonthAccountAdapter.MonthAccountViewHolder> {
    private final List<MonthAccountInfo> monthAccountInfoList;  //每月流水数据列表
    private MonthAccountInfoType monthAccountInfoType;          //显示的每月流水数据的种类
    private final Context context;                              //上下文

    public static class MonthAccountViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView month_name_text;   //月份文本
        MaterialTextView proportion_text;   //金额占比文本
        MaterialTextView amount_text;       //金额文本
        ProgressBar proportion_bar;         //占比进度条

        public MonthAccountViewHolder(@NonNull View itemView) {
            super(itemView);

            month_name_text = itemView.findViewById(R.id.source_name_text);
            proportion_text = itemView.findViewById(R.id.percentage_text);
            amount_text = itemView.findViewById(R.id.amount_text);
            proportion_bar = itemView.findViewById(R.id.percentage_bar);
        }
    }

    public MonthAccountAdapter(List<MonthAccountInfo> monthAccountInfoList, MonthAccountInfoType monthAccountInfoType, Context context) {
        this.monthAccountInfoList = monthAccountInfoList;
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
                    int color_err = ColorHelper.getAttrColor(context, androidx.appcompat.R.attr.colorError);
                    holder.proportion_bar.setProgressTintList(ColorStateList.valueOf(color_err));
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

        holder.month_name_text.setText(month_name);                 //月份名称
        holder.amount_text.setText(String.format("%.2f", amount));  //金额
        String percentage_str = String.format("%d%%", percentage);
        holder.proportion_text.setText(percentage_str);             //百分比文本
        holder.proportion_bar.setProgress(percentage);              //百分比进度条
    }

    @Override
    public int getItemCount() {
        return monthAccountInfoList.size();
    }

    /**
     * 每月流水信息类型改变时刷新视图
     *
     * @param type 新类型
     */
    @SuppressLint("NotifyDataSetChanged")
    public void onMonthAccountInfoTypeChanged(MonthAccountInfoType type) {
        this.monthAccountInfoType = type;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void onYearChanged() {
        notifyDataSetChanged();
    }
}
