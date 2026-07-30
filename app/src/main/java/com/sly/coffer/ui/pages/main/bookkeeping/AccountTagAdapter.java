package com.sly.coffer.ui.pages.main.bookkeeping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.sly.coffer.auxiliary.interfaces.adapter.AdapterOnChipCloseListener;
import com.sly.coffer.auxiliary.interfaces.adapter.ChipViewHolderListener;
import com.sly.coffer.data.save.db.entities.TagEntity;
import com.sly.coffer.databinding.ViewHolderChipTextBinding;

public class AccountTagAdapter extends ListAdapter<TagEntity, AccountTagAdapter.TagViewHolder> {
    private static final DiffUtil.ItemCallback<TagEntity> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TagEntity oldItem, @NonNull TagEntity newItem) {
            return oldItem.getTagId() == newItem.getTagId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TagEntity oldItem, @NonNull TagEntity newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    };
    private final AdapterOnChipCloseListener<TagEntity, AccountTagAdapter> closeListener;

    public static class TagViewHolder extends RecyclerView.ViewHolder {
        ViewHolderChipTextBinding binding;

        public TagViewHolder(@NonNull ViewHolderChipTextBinding binding, ChipViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            binding.chip.setCloseIconVisible(true);

            //关闭监听
            binding.chip.setOnCloseIconClickListener(view ->
                    listener.onClose(getBindingAdapterPosition(), binding.getRoot())
            );
        }
    }

    /**
     * @param closeListener 关闭图标按钮点击监听
     */
    public AccountTagAdapter(AdapterOnChipCloseListener<TagEntity, AccountTagAdapter> closeListener) {
        super(ITEM_CALLBACK);
        this.closeListener = closeListener;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderChipTextBinding binding = ViewHolderChipTextBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new TagViewHolder(
                binding,
                new ChipViewHolderListener() {
                    @Override
                    public void onClick(int pos, View anchor) {
                    }

                    @Override
                    public void onClose(int pos, View anchor) {
                        TagEntity tag = getItem(pos);
                        closeListener.onClose(tag, anchor, AccountTagAdapter.this);
                    }

                    @Override
                    public void onCheckedChanged(int pos, boolean isChecked, View anchor) {
                    }
                }
        );
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        TagEntity tag = getItem(position);
        holder.binding.chip.setText(tag.getName());
    }
}
