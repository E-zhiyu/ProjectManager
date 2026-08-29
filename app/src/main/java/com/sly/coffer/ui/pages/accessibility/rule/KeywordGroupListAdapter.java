package com.sly.coffer.ui.pages.accessibility.rule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.sly.coffer.auxiliary.interfaces.adapter.AdapterOnChipCloseListener;
import com.sly.coffer.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.sly.coffer.auxiliary.interfaces.adapter.ChipViewHolderListener;
import com.sly.coffer.data.save.db.entities.AccessibilityRuleKeywordGroupEntity;
import com.sly.coffer.databinding.ViewHolderChipTextBinding;

public class KeywordGroupListAdapter extends ListAdapter<AccessibilityRuleKeywordGroupEntity, KeywordGroupListAdapter.ItemViewHolder> {
    private static final DiffUtil.ItemCallback<AccessibilityRuleKeywordGroupEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull AccessibilityRuleKeywordGroupEntity oldItem, @NonNull AccessibilityRuleKeywordGroupEntity newItem) {
            return oldItem.getKeywordId() == newItem.getKeywordId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull AccessibilityRuleKeywordGroupEntity oldItem, @NonNull AccessibilityRuleKeywordGroupEntity newItem) {
            return oldItem.getContent().equals(newItem.getContent());
        }
    };
    private final AdapterOnChipCloseListener<AccessibilityRuleKeywordGroupEntity, KeywordGroupListAdapter> closeListener;
    private final AdapterOnClickListener<AccessibilityRuleKeywordGroupEntity> clickListener;

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ViewHolderChipTextBinding binding;

        public ItemViewHolder(@NonNull ViewHolderChipTextBinding binding, ChipViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            binding.chip.setCloseIconVisible(true);

            //点击监听
            binding.chip.setOnClickListener(view ->
                    listener.onClick(getBindingAdapterPosition(), binding.getRoot())
            );

            //关闭监听
            binding.chip.setOnCloseIconClickListener(view ->
                    listener.onClose(getBindingAdapterPosition(), binding.getRoot())
            );
        }
    }

    protected KeywordGroupListAdapter(AdapterOnChipCloseListener<AccessibilityRuleKeywordGroupEntity, KeywordGroupListAdapter> closeListener, AdapterOnClickListener<AccessibilityRuleKeywordGroupEntity> clickListener) {
        super(ITEM_CALLBACK);
        this.closeListener = closeListener;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderChipTextBinding binding = ViewHolderChipTextBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ItemViewHolder(
                binding,
                new ChipViewHolderListener() {
                    @Override
                    public void onClick(int pos, View anchor) {
                        AccessibilityRuleKeywordGroupEntity entity = getItem(pos);
                        clickListener.onClick(entity, anchor);
                    }

                    @Override
                    public void onClose(int pos, View anchor) {
                        AccessibilityRuleKeywordGroupEntity entity = getItem(pos);
                        closeListener.onClose(entity, anchor, KeywordGroupListAdapter.this);
                    }

                    @Override
                    public void onCheckedChanged(int pos, boolean isChecked, View anchor) {
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        AccessibilityRuleKeywordGroupEntity entity = getItem(position);

        String content = entity.getContent();
        String[] parts = content.split("\\s+");
        StringBuilder display = new StringBuilder();
        if (parts.length <= 3) {
            for (int i = 0; i < parts.length; i++) {
                display.append(parts[i]);
                if (i < parts.length - 1) {
                    display.append("、");
                }
            }
        } else {
            display.append(parts[0]).append("、");
            display.append(parts[1]).append("、");
            display.append(parts[2]).append("、");
            display.append("……");
        }

        holder.binding.chip.setText(display.toString());
    }
}
