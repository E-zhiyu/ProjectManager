package com.manager.assistant.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.databinding.ViewHolderSearchHistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.SearchHistoryViewHolder> {
    private final List<String> searchHistoryList = new ArrayList<>();   //搜索历史记录列表
    private final OnClickerListener listener;                           //视图的点击监听

    /**
     * 搜索历史适配器构造方法
     *
     * @param listener 搜索历史Chip点击监听器
     */
    public SearchHistoryAdapter(OnClickerListener listener) {
        this.listener = listener;
    }

    public static class SearchHistoryViewHolder extends RecyclerView.ViewHolder {
        ViewHolderSearchHistoryBinding binding;

        public SearchHistoryViewHolder(@NonNull ViewHolderSearchHistoryBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            binding.titleChip.setOnClickListener(v -> listener.onClicked(getBindingAdapterPosition()));
        }
    }

    public interface OnClickerListener {
        /**
         * 搜索历史记录点击监听
         *
         * @param keyWord 点击的关键词
         */
        void onClicked(String keyWord);
    }

    public interface ViewHolderListener {
        /**
         * 当ViewHolder被点击时的监听器
         *
         * @param position 被点击的ViewHolder在Adapter中的真实下标
         */
        void onClicked(int position);
    }

    @NonNull
    @Override
    public SearchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderSearchHistoryBinding binding = ViewHolderSearchHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SearchHistoryViewHolder(binding, position -> {
            String historyKeyWord = searchHistoryList.get(position);
            listener.onClicked(historyKeyWord);
        });
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistoryViewHolder holder, int position) {
        String historyKeyWord = searchHistoryList.get(position);
        holder.binding.titleChip.setText(historyKeyWord);
    }

    @Override
    public int getItemCount() {
        return searchHistoryList.size();
    }

    /**
     * 刷新搜索历史记录
     *
     * @param searchHistoryList 刷新后的搜索历史记录列表
     */
    public void refreshSearchHistory(List<String> searchHistoryList) {
        int oldSize = this.searchHistoryList.size();
        this.searchHistoryList.clear();
        notifyItemRangeRemoved(0, oldSize);

        this.searchHistoryList.addAll(searchHistoryList);
        notifyItemRangeInserted(0, searchHistoryList.size());
    }
}
