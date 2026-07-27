package com.sly.coffer.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.sly.coffer.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.sly.coffer.auxiliary.interfaces.adapter.ViewHolderListener;
import com.sly.coffer.data.save.preference.SearchHistoryPreference;
import com.sly.coffer.databinding.ViewHolderChipTextBinding;

import java.util.List;

public class SearchHistoryAdapter extends ListAdapter<String, SearchHistoryAdapter.SearchHistoryViewHolder> {
    private final static DiffUtil.ItemCallback<String> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return true;
        }
    };
    private final String key;                                   //保存搜索历史的关键字
    private final AdapterOnClickListener<String> clickListener;
    private final OnHistoryListChangListener changeListener;

    public interface OnHistoryListChangListener {
        void onChange(List<String> keywordList, SearchHistoryAdapter adapter);
    }

    /**
     * 搜索历史适配器构造方法
     *
     * @param key           保存搜索关键词的键，详见{@link SearchHistoryPreference}的静态字符串
     * @param clickListener 搜索历史 Chip 点击监听器
     */
    public SearchHistoryAdapter(
            String key,
            AdapterOnClickListener<String> clickListener,
            OnHistoryListChangListener changeListener
    ) {
        super(ITEM_CALLBACK);
        this.key = key;
        this.clickListener = clickListener;
        this.changeListener = changeListener;
    }

    public static class SearchHistoryViewHolder extends RecyclerView.ViewHolder {
        ViewHolderChipTextBinding binding;

        public SearchHistoryViewHolder(@NonNull ViewHolderChipTextBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置不可选择
            binding.chip.setCheckable(false);

            //设置点击监听
            binding.chip.setOnClickListener(v -> listener.onClick(getBindingAdapterPosition(), binding.getRoot()));

            //设置长按监听
            binding.chip.setOnLongClickListener(view -> {
                listener.onLongClick(getBindingAdapterPosition(), binding.getRoot());
                return true;
            });
        }
    }

    @NonNull
    @Override
    public SearchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderChipTextBinding binding = ViewHolderChipTextBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SearchHistoryViewHolder(
                binding,
                new ViewHolderListener() {
                    @Override
                    public void onClick(int position, View anchor) {
                        String historyKeyWord = getItem(position);
                        clickListener.onClick(historyKeyWord, anchor);

                        //将点击的关键词放到第一位
                        List<String> historyList = SearchHistoryPreference.addKeyword(
                                historyKeyWord,
                                key,
                                parent.getContext()
                        );
                        changeListener.onChange(historyList, SearchHistoryAdapter.this);
                    }

                    @Override
                    public void onLongClick(int position, View anchor) {
                        String historyKeyWord = getItem(position);

                        //将点击的关键词放到第一位
                        List<String> historyList = SearchHistoryPreference.removeKeyword(
                                historyKeyWord,
                                key,
                                parent.getContext()
                        );
                        changeListener.onChange(historyList, SearchHistoryAdapter.this);
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistoryViewHolder holder, int position) {
        String historyKeyWord = getItem(position);
        holder.binding.chip.setText(historyKeyWord);
    }

    public void submitListWithAnimation(List<String> keywordList) {
        changeListener.onChange(keywordList, SearchHistoryAdapter.this);
    }
}
