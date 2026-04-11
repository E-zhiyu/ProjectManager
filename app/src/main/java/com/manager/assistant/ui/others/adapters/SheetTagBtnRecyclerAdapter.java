package com.manager.assistant.ui.others.adapters;

import android.content.Context;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.databinding.ViewHolderTagBtnBinding;
import com.manager.assistant.ui.others.listeners.SpringAnimationOnTouchListener;

import java.util.List;

public class SheetTagBtnRecyclerAdapter extends RecyclerView.Adapter<SheetTagBtnRecyclerAdapter.BtnViewHolder> {
    private final List<Tag> tagList;                                //标签数据源列表
    private final OnTagBtnClickedListener tagBtnClickedListener;    //标签按钮点击监听器

    //标签按钮点击监听接口
    public interface OnTagBtnClickedListener {
        void onTagBtnClicked(long tagNo, String tagName);   //传递标签编号和名称
    }

    public static class BtnViewHolder extends RecyclerView.ViewHolder {
        ViewHolderTagBtnBinding binding;
        SpringAnimationOnTouchListener onTouchListener; //带有点击动画的监听器

        public BtnViewHolder(@NonNull ViewHolderTagBtnBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸监听器
            Vibrator vibrator = (Vibrator) binding.getRoot().getContext()
                    .getSystemService(Context.VIBRATOR_SERVICE);
            onTouchListener = new SpringAnimationOnTouchListener(binding.getRoot(), vibrator);
            binding.getRoot().setOnTouchListener(onTouchListener);
        }
    }

    public SheetTagBtnRecyclerAdapter(List<Tag> tagList, OnTagBtnClickedListener tagBtnClickedListener) {
        this.tagList = tagList;
        this.tagBtnClickedListener = tagBtnClickedListener;
    }

    @NonNull
    @Override
    public BtnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderTagBtnBinding binding = ViewHolderTagBtnBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new BtnViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BtnViewHolder holder, int position) {
        Tag oneTag = tagList.get(position);
        String tagName = oneTag.getName();
        long tagTno = oneTag.getTno();

        holder.binding.tagBtn.setText(tagName);

        holder.binding.tagBtn.setOnClickListener(v -> tagBtnClickedListener.onTagBtnClicked(tagTno, tagName));
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }
}
