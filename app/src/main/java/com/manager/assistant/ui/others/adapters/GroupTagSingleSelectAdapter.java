package com.manager.assistant.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.manager.assistant.auxiliary.interfaces.adapter.AdapterOnClickListener;
import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.entities.composite.ui.TagGroupUiModel;
import com.manager.assistant.databinding.ViewHolderGroupTagItemBinding;
import com.manager.assistant.databinding.ViewHolderSeparatorTextviewBinding;

import java.util.List;
import java.util.stream.Collectors;

public class GroupTagSingleSelectAdapter extends ListAdapter <TagGroupUiModel, RecyclerView.ViewHolder> {
    private static final DiffUtil.ItemCallback<TagGroupUiModel> ITEM_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull TagGroupUiModel oldItem, @NonNull TagGroupUiModel newItem) {
            if (oldItem instanceof TagGroupUiModel.Item && newItem instanceof TagGroupUiModel.Item) {
                TagGroupUiModel.Item oldI = (TagGroupUiModel.Item) oldItem;
                TagGroupUiModel.Item newI = (TagGroupUiModel.Item) newItem;
                List<Long> oldItemRoleIdList = oldI.tagList.stream()
                        .map(TagEntity::getTagId)
                        .collect(Collectors.toList());
                List<Long> newItemRoleIdList = newI.tagList.stream()
                        .map(TagEntity::getTagId)
                        .collect(Collectors.toList());
                return oldItemRoleIdList.equals(newItemRoleIdList);
            } else if (oldItem instanceof TagGroupUiModel.Separator && newItem instanceof TagGroupUiModel.Separator) {
                TagGroupUiModel.Separator oldS = (TagGroupUiModel.Separator) oldItem;
                TagGroupUiModel.Separator newS = (TagGroupUiModel.Separator) newItem;
                return oldS.separatorText.equals(newS.separatorText);
            } else {
                return false;
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull TagGroupUiModel oldItem, @NonNull TagGroupUiModel newItem) {
            if (oldItem instanceof TagGroupUiModel.Item && newItem instanceof TagGroupUiModel.Item) {
                return true;
            } else
                return oldItem instanceof TagGroupUiModel.Separator && newItem instanceof TagGroupUiModel.Separator;
        }
    };
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_SEPARATOR = 0;
    private final AdapterOnClickListener<TagEntity> clickListener;

    public static class GroupRoleItemViewHolder extends RecyclerView.ViewHolder {
        ViewHolderGroupTagItemBinding binding;

        public GroupRoleItemViewHolder(@NonNull ViewHolderGroupTagItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * 刷新 ChipGroup 中的标签 Chip
         *
         * @param listener 标签点击后触发的监听器
         */
        public void refreshRoleChip(@NonNull List<TagEntity> tagList, AdapterOnClickListener<TagEntity> listener) {
            //删除之前的视图
            binding.chipGroup.removeAllViews();

            //添加新的视图
            for (TagEntity tag : tagList) {
                //实例化 Chip
                Chip chip = new Chip(binding.getRoot().getContext());
                chip.setCheckable(false);

                //设置显示名称
                chip.setText(tag.getName());

                //添加到视图
                binding.chipGroup.addView(chip);

                //绑定点击监听器
                chip.setOnClickListener(v ->
                        listener.onClick(tag, binding.getRoot())
                );
            }
        }
    }

    public static class GroupRoleSeparatorViewHolder extends RecyclerView.ViewHolder {
        ViewHolderSeparatorTextviewBinding binding;

        public GroupRoleSeparatorViewHolder(@NonNull ViewHolderSeparatorTextviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public GroupTagSingleSelectAdapter(AdapterOnClickListener<TagEntity> clickListener) {
        super(ITEM_CALLBACK);
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        TagGroupUiModel item = getItem(position);
        if (item instanceof TagGroupUiModel.Item) return TYPE_ITEM;
        return TYPE_SEPARATOR;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            ViewHolderGroupTagItemBinding binding = ViewHolderGroupTagItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new GroupRoleItemViewHolder(binding);
        } else {
            ViewHolderSeparatorTextviewBinding binding = ViewHolderSeparatorTextviewBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new GroupRoleSeparatorViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TagGroupUiModel dataItem = getItem(position);
        if (dataItem instanceof TagGroupUiModel.Item && holder instanceof GroupTagSingleSelectAdapter.GroupRoleItemViewHolder) {
            TagGroupUiModel.Item item = (TagGroupUiModel.Item) dataItem;
            GroupTagSingleSelectAdapter.GroupRoleItemViewHolder itemHolder = (GroupTagSingleSelectAdapter.GroupRoleItemViewHolder) holder;

            itemHolder.refreshRoleChip(item.tagList, clickListener);
        } else if (dataItem instanceof TagGroupUiModel.Separator && holder instanceof GroupTagSingleSelectAdapter.GroupRoleSeparatorViewHolder) {
            TagGroupUiModel.Separator separator = (TagGroupUiModel.Separator) dataItem;
            GroupTagSingleSelectAdapter.GroupRoleSeparatorViewHolder separatorHolder = (GroupTagSingleSelectAdapter.GroupRoleSeparatorViewHolder) holder;

            separatorHolder.binding.text.setText(separator.separatorText);
        }
    }
}
