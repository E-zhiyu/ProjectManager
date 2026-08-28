package com.sly.coffer.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.sly.coffer.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.sly.coffer.data.save.db.entities.PickedPageEntity;
import com.sly.coffer.data.save.db.entities.composite.ui.PickedPageGroupUiModel;
import com.sly.coffer.databinding.ViewHolderGroupPickedPageBinding;
import com.sly.coffer.databinding.ViewHolderSeparatorTextviewBinding;

import java.util.List;
import java.util.stream.Collectors;

public class GroupPickedPageSelectAdapter extends ListAdapter<PickedPageGroupUiModel, RecyclerView.ViewHolder> {
    private final static DiffUtil.ItemCallback<PickedPageGroupUiModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull PickedPageGroupUiModel oldItem, @NonNull PickedPageGroupUiModel newItem) {
            if (oldItem instanceof PickedPageGroupUiModel.Item && newItem instanceof PickedPageGroupUiModel.Item) {
                List<Long> oldIdList = ((PickedPageGroupUiModel.Item) oldItem).viewList.stream()
                        .map(PickedPageEntity::getId)
                        .collect(Collectors.toList());
                List<Long> newIdList = ((PickedPageGroupUiModel.Item) newItem).viewList.stream()
                        .map(PickedPageEntity::getId)
                        .collect(Collectors.toList());
                return oldIdList.equals(newIdList);
            } else if (oldItem instanceof PickedPageGroupUiModel.Separator && newItem instanceof PickedPageGroupUiModel.Separator) {
                return ((PickedPageGroupUiModel.Separator) oldItem).text.equals(((PickedPageGroupUiModel.Separator) newItem).text);
            } else {
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull PickedPageGroupUiModel oldItem, @NonNull PickedPageGroupUiModel newItem) {
            if (oldItem instanceof PickedPageGroupUiModel.Item && newItem instanceof PickedPageGroupUiModel.Item) {
                return true;
            } else
                return oldItem instanceof PickedPageGroupUiModel.Separator && newItem instanceof PickedPageGroupUiModel.Separator;
        }
    };

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_SEPARATOR = 0;
    private final AdapterOnClickListener<PickedPageEntity> clickListener;

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ViewHolderGroupPickedPageBinding binding;

        public ItemViewHolder(@NonNull ViewHolderGroupPickedPageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * 刷新 ChipGroup 中的标签 Chip
         *
         * @param listener 标签点击后触发的监听器
         */
        public void refreshRoleChip(@NonNull List<PickedPageEntity> tagList, AdapterOnClickListener<PickedPageEntity> listener) {
            //删除之前的视图
            binding.chipGroup.removeAllViews();

            //添加新的视图
            for (PickedPageEntity view : tagList) {
                //实例化 Chip
                Chip chip = new Chip(binding.getRoot().getContext());
                chip.setCheckable(false);

                //设置显示名称
                chip.setText(view.getRemark());

                //添加到视图
                binding.chipGroup.addView(chip);

                //绑定点击监听器
                chip.setOnClickListener(v ->
                        listener.onClick(view, binding.getRoot())
                );
            }
        }
    }

    public static class SeparatorViewHolder extends RecyclerView.ViewHolder {
        ViewHolderSeparatorTextviewBinding binding;

        public SeparatorViewHolder(@NonNull ViewHolderSeparatorTextviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public GroupPickedPageSelectAdapter(AdapterOnClickListener<PickedPageEntity> clickListener) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        PickedPageGroupUiModel item = getItem(position);
        if (item instanceof PickedPageGroupUiModel.Item) return TYPE_ITEM;
        return TYPE_SEPARATOR;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            ViewHolderGroupPickedPageBinding binding = ViewHolderGroupPickedPageBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new ItemViewHolder(binding);
        } else {
            ViewHolderSeparatorTextviewBinding binding = ViewHolderSeparatorTextviewBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new SeparatorViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PickedPageGroupUiModel dataItem = getItem(position);
        if (dataItem instanceof PickedPageGroupUiModel.Item && holder instanceof ItemViewHolder) {
            PickedPageGroupUiModel.Item item = (PickedPageGroupUiModel.Item) dataItem;
            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            itemHolder.refreshRoleChip(item.viewList, clickListener);
        } else if (dataItem instanceof PickedPageGroupUiModel.Separator && holder instanceof SeparatorViewHolder) {
            PickedPageGroupUiModel.Separator separator = (PickedPageGroupUiModel.Separator) dataItem;
            SeparatorViewHolder separatorHolder = (SeparatorViewHolder) holder;

            separatorHolder.binding.text.setText(separator.text);
        }
    }
}
