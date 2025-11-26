package com.project.manager.ui.bookkeeping.report;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;

import java.util.List;

public class AccountSourceAdapter extends RecyclerView.Adapter<AccountSourceAdapter.AccountProportionViewHolder> {
    private final List<AccountSourceInfo> sourceCardList;  //来源卡片列表

    public static class AccountProportionViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView source_name_text;  //标签名称文本
        MaterialTextView proportion_text;   //金额占比文本
        MaterialTextView amount_text;       //金额文本
        ProgressBar proportion_bar;         //占比进度条

        public AccountProportionViewHolder(@NonNull View itemView) {
            super(itemView);

            source_name_text = itemView.findViewById(R.id.source_name_text);
            proportion_text = itemView.findViewById(R.id.percentage_text);
            amount_text = itemView.findViewById(R.id.amount_text);
            proportion_bar = itemView.findViewById(R.id.percentage_bar);
        }
    }

    public AccountSourceAdapter(List<AccountSourceInfo> sourceCardList) {
        this.sourceCardList = sourceCardList;
    }

    @NonNull
    @Override
    public AccountProportionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_amount_proportion, parent, false);
        return new AccountProportionViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return sourceCardList.size();
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void onBindViewHolder(@NonNull AccountProportionViewHolder holder, int position) {
        AccountSourceInfo oneSourceInfo = sourceCardList.get(position);
        String source_name = oneSourceInfo.getSource_name();
        int percentage = oneSourceInfo.getPercentage();
        double amount = oneSourceInfo.getAmount();

        holder.source_name_text.setText(source_name);               //来源名称
        holder.amount_text.setText(String.format("%.2f", amount));  //金额
        String percentage_str = String.format("%d%%", percentage);
        holder.proportion_text.setText(percentage_str);             //百分比文本
        holder.proportion_bar.setProgress(percentage);              //百分比进度条
    }
}
