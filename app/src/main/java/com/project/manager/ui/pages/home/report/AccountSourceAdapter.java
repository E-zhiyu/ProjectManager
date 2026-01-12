package com.project.manager.ui.pages.home.report;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.data.data_class.AccountSourceInfo;

import java.util.List;

public class AccountSourceAdapter extends RecyclerView.Adapter<AccountSourceAdapter.AccountProportionViewHolder> {
    private final List<AccountSourceInfo> sourceCardList;  //来源卡片列表

    public static class AccountProportionViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView sourceNameText;        //标签名称文本
        MaterialTextView proportionText;        //金额占比文本
        MaterialTextView amountText;            //金额文本
        LinearProgressIndicator proportionBar;  //占比进度条

        public AccountProportionViewHolder(@NonNull View itemView) {
            super(itemView);

            sourceNameText = itemView.findViewById(R.id.source_name_text);
            proportionText = itemView.findViewById(R.id.percentage_text);
            amountText = itemView.findViewById(R.id.amount_text);
            proportionBar = itemView.findViewById(R.id.percentage_bar);
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

        holder.sourceNameText.setText(source_name);               //来源名称
        holder.amountText.setText(String.format("%.2f", amount));  //金额
        String percentage_str = String.format("%d%%", percentage);
        holder.proportionText.setText(percentage_str);             //百分比文本
        holder.proportionBar.setProgress(percentage);              //百分比进度条
    }
}
