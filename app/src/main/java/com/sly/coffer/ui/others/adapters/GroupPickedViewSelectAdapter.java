package com.sly.coffer.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.sly.coffer.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.sly.coffer.data.save.db.entities.PickedViewEntity;
import com.sly.coffer.data.save.db.entities.composite.ui.PickedViewGroupUiModel;
import com.sly.coffer.databinding.ViewHolderGroupPickedViewBinding;
import com.sly.coffer.databinding.ViewHolderSeparatorTextviewBinding;

import java.util.List;
import java.util.stream.Collectors;

public class GroupPickedViewSelectAdapter extends ListAdapter<PickedViewGroupUiModel, RecyclerView.ViewHolder> {
    private final static DiffUtil.ItemCallback<PickedViewGroupUiModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull PickedViewGroupUiModel oldItem, @NonNull PickedViewGroupUiModel newItem) {
            if (oldItem instanceof PickedViewGroupUiModel.Item && newItem instanceof PickedViewGroupUiModel.Item) {
                List<Long> oldIdList = ((PickedViewGroupUiModel.Item) oldItem).viewList.stream()
                        .map(PickedViewEntity::getId)
                        .collect(Collectors.toList());
                List<Long> newIdList = ((PickedViewGroupUiModel.Item) newItem).viewList.stream()
                        .map(PickedViewEntity::getId)
                        .collect(Collectors.toList());
                return oldIdList.equals(newIdList);
            } else if (oldItem instanceof PickedViewGroupUiModel.Separator && newItem instanceof PickedViewGroupUiModel.Separator) {
                return ((PickedViewGroupUiModel.Separator) oldItem).text.equals(((PickedViewGroupUiModel.Separator) newItem).text);
            } else {
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull PickedViewGroupUiModel oldItem, @NonNull PickedViewGroupUiModel newItem) {
            if (oldItem instanceof PickedViewGroupUiModel.Item && newItem instanceof PickedViewGroupUiModel.Item) {
                return true;
            } else
                return oldItem instanceof PickedViewGroupUiModel.Separator && newItem instanceof PickedViewGroupUiModel.Separator;
        }
    };

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_SEPARATOR = 0;
    private final AdapterOnClickListener<PickedViewEntity> clickListener;

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ViewHolderGroupPickedViewBinding binding;

        public ItemViewHolder(@NonNull ViewHolderGroupPickedViewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * 刷新 ChipGroup 中的标签 Chip
         *
         * @param listener 标签点击后触发的监听器
         */
        public void refreshRoleChip(@NonNull List<PickedViewEntity> tagList, AdapterOnClickListener<PickedViewEntity> listener) {
            //删除之前的视图
            binding.chipGroup.removeAllViews();

            //添加新的视图
            for (PickedViewEntity view : tagList) {
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

    public GroupPickedViewSelectAdapter(AdapterOnClickListener<PickedViewEntity> clickListener) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        PickedViewGroupUiModel item = getItem(position);
        if (item instanceof PickedViewGroupUiModel.Item) return TYPE_ITEM;
        return TYPE_SEPARATOR;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            ViewHolderGroupPickedViewBinding binding = ViewHolderGroupPickedViewBinding.inflate(
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
        PickedViewGroupUiModel dataItem = getItem(position);
        if (dataItem instanceof PickedViewGroupUiModel.Item && holder instanceof ItemViewHolder) {
            PickedViewGroupUiModel.Item item = (PickedViewGroupUiModel.Item) dataItem;
            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            itemHolder.refreshRoleChip(item.viewList, clickListener);
        } else if (dataItem instanceof PickedViewGroupUiModel.Separator && holder instanceof SeparatorViewHolder) {
            PickedViewGroupUiModel.Separator separator = (PickedViewGroupUiModel.Separator) dataItem;
            SeparatorViewHolder separatorHolder = (SeparatorViewHolder) holder;

            separatorHolder.binding.text.setText(separator.text);
        }
    }
}
