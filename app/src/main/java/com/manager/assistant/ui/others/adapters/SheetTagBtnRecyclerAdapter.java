package com.manager.assistant.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.databinding.ViewHolderTagBtnBinding;
import com.manager.assistant.helpers.appearence.AppearanceHelper;

import java.util.List;

public class SheetTagBtnRecyclerAdapter extends RecyclerView.Adapter<SheetTagBtnRecyclerAdapter.BtnViewHolder> {
    private final List<Tag> tagList;                //标签数据源列表
    private final OnTagBtnClickedListener listener; //标签按钮点击监听器

    public interface OnTagBtnClickedListener {
        /**
         * 标签按钮点击监听
         *
         * @param tagNo   点击的标签编号
         * @param tagName 点击的标签名称
         */
        void onTagBtnClicked(long tagNo, String tagName);   //传递标签编号和名称
    }

    public interface ViewHolderListener {
        /**
         * 当ViewHolder被点击时的监听器
         *
         * @param position 被点击的ViewHolder在Adapter中的真实下标
         */
        void onClicked(int position);
    }

    public static class BtnViewHolder extends RecyclerView.ViewHolder {
        ViewHolderTagBtnBinding binding;

        public BtnViewHolder(@NonNull ViewHolderTagBtnBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸监听器
            AppearanceHelper.attachMorphAnimation(binding.getRoot());

            //设置按钮的点击监听
            binding.tagBtn.setOnClickListener(v -> listener.onClicked(getBindingAdapterPosition()));
        }
    }

    public SheetTagBtnRecyclerAdapter(List<Tag> tagList, OnTagBtnClickedListener listener) {
        this.tagList = tagList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BtnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderTagBtnBinding binding = ViewHolderTagBtnBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new BtnViewHolder(binding, position -> {
            if (position != RecyclerView.NO_POSITION) {
                Tag tag = tagList.get(position);
                listener.onTagBtnClicked(tag.getTno(), tag.getName());
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull BtnViewHolder holder, int position) {
        Tag oneTag = tagList.get(position);
        String tagName = oneTag.getName();
        holder.binding.tagBtn.setText(tagName);
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }
}
