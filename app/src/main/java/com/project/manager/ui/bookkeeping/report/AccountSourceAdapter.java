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

public class AccountSourceAdapter extends RecyclerView.Adapter<AccountSourceAdapter.AccountScourceViewHolder> {
    private final List<AccountSourceCard> sourceCardList;  //来源卡片列表

    public static class AccountScourceViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView source_name_text;  //标签名称文本
        MaterialTextView percentage_text;   //金额占比文本
        ProgressBar percentage_bar;         //占比进度条

        public AccountScourceViewHolder(@NonNull View itemView) {
            super(itemView);

            source_name_text = itemView.findViewById(R.id.source_name_text);
            percentage_text = itemView.findViewById(R.id.percentage_text);
            percentage_bar = itemView.findViewById(R.id.percentage_bar);
        }
    }

    public AccountSourceAdapter(List<AccountSourceCard> sourceCardList) {
        this.sourceCardList = sourceCardList;
    }

    @NonNull
    @Override
    public AccountScourceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_account_source, parent, false);
        return new AccountScourceViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return sourceCardList.size();
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void onBindViewHolder(@NonNull AccountScourceViewHolder holder, int position) {
        AccountSourceCard oneSourceCard = sourceCardList.get(position);
        String source_name = oneSourceCard.getSource_name();
        int percentage = oneSourceCard.getPercentage();

        holder.source_name_text.setText(source_name);   //来源名称
        String percentage_str = String.format("%d%%", percentage);
        holder.percentage_text.setText(percentage_str); //百分比文本
        holder.percentage_bar.setProgress(percentage);  //占比进度条
    }
}
