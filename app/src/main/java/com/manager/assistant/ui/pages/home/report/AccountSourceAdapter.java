package com.manager.assistant.ui.pages.home.report;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.data.classes.AccountSourceInfo;
import com.manager.assistant.databinding.ViewHolderAmountProportionBinding;
import com.manager.assistant.ui.others.animators.StrikeThroughAnimator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountSourceAdapter extends RecyclerView.Adapter<AccountSourceAdapter.AccountProportionViewHolder> {
    private final List<AccountSourceInfo> sourceInfoList = new ArrayList<>();   //来源卡片列表
    private final OnSourceItemClickedListener listener;

    public interface OnSourceItemClickedListener {
        /**
         * 收支来源项点击监听
         *
         * @param sourceInfo 被点击的视图对应的收支来源数据
         * @param isExcepted 点击后是否需要被排除
         */
        void onClicked(AccountSourceInfo sourceInfo, boolean isExcepted);
    }

    public static class AccountProportionViewHolder extends RecyclerView.ViewHolder {
        ViewHolderAmountProportionBinding binding;
        boolean isExcepted = false;               //在统计报表时该项是否被排除

        public AccountProportionViewHolder(@NonNull ViewHolderAmountProportionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public AccountSourceAdapter(List<AccountSourceInfo> sourceInfoList, OnSourceItemClickedListener listener) {
        this.sourceInfoList.addAll(sourceInfoList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccountProportionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderAmountProportionBinding binding = ViewHolderAmountProportionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new AccountProportionViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return sourceInfoList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull AccountProportionViewHolder holder, int position) {
        AccountSourceInfo info = sourceInfoList.get(position);
        String source_name = info.getName();
        int percentage = info.getPercentage();
        double amount = info.getAmount();

        holder.binding.sourceNameText.setText(source_name);               //来源名称
        holder.binding.amountText.setText(String.format(Locale.getDefault(), "%.2f", amount));  //金额
        String percentageStr = String.format(Locale.getDefault(), "%d%%", percentage);
        holder.binding.percentageText.setText(percentageStr);             //百分比文本
        holder.binding.percentageBar.setProgress(percentage);             //百分比进度条

        //设置点击监听
        StrikeThroughAnimator strikeThroughAnimator = new StrikeThroughAnimator(
                holder.binding.sourceNameText,
                holder.itemView.getContext()
        );
        holder.itemView.setOnClickListener(v -> {
            holder.isExcepted = !holder.isExcepted;
            listener.onClicked(info, holder.isExcepted);

            //添加/删除删除线
            strikeThroughAnimator.setExcluded(holder.isExcepted);
        });
    }

    /**
     * 刷新收支来源
     *
     * @param sourceInfoList 刷新后的收支来源数据
     */
    @SuppressLint("NotifyDataSetChanged")
    public void refreshSource(List<AccountSourceInfo> sourceInfoList) {
        this.sourceInfoList.clear();
        this.sourceInfoList.addAll(sourceInfoList);

        //只有在新的列表不为空时才更新内容，防止内容先消失然后再播放缩小动画
        if (!sourceInfoList.isEmpty()) {
            notifyDataSetChanged();
        }
    }
}
