package com.manager.assistant.ui.others.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.databinding.ViewHolderMultichoiceItemBinding;

public class MultiChoiceDialogAdapter extends RecyclerView.Adapter<MultiChoiceDialogAdapter.MultiChoiceItemViewHolder> {
    private final String[] itemNames;           //多选选项
    private final boolean[] itemStats;          //选项初始状态
    private final boolean[] itemEnabled;        //选项是否被禁用
    private final OnCheckedListener listener;   //选择行为监听器

    public static class MultiChoiceItemViewHolder extends RecyclerView.ViewHolder {
        ViewHolderMultichoiceItemBinding binding;

        public MultiChoiceItemViewHolder(@NonNull ViewHolderMultichoiceItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnCheckedListener {
        /**
         * 复选框状态变化回调
         *
         * @param position  复选框的下标
         * @param isChecked 改变后的状态
         */
        void onChecked(int position, boolean isChecked);
    }

    /**
     * 不设置是否启用可选项的构造方法
     *
     * @param itemEnabled 选项是否启用(为null则不禁用任何选项)
     * @param itemStats   选项初始状态
     * @param itemNames   选项名称
     * @param listener    选项点击监听器
     */
    public MultiChoiceDialogAdapter(@Nullable boolean[] itemEnabled, boolean[] itemStats, String[] itemNames, OnCheckedListener listener) {
        this.itemEnabled = itemEnabled;
        this.itemStats = itemStats;
        this.itemNames = itemNames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MultiChoiceItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderMultichoiceItemBinding binding = ViewHolderMultichoiceItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new MultiChoiceItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiChoiceItemViewHolder holder, int position) {
        boolean isEnabled = true;
        if (itemEnabled != null) {
            isEnabled = itemEnabled[position];
        }
        boolean stat = itemStats[position];

        //设置选项的可用性
        holder.binding.checkedText.setText(itemNames[position]);
        if (!isEnabled) {
            //禁用选项
            holder.binding.checkedText.setEnabled(false);
            holder.binding.checkedText.setChecked(false);

            //图标也变成灰色
            TextViewCompat.setCompoundDrawableTintList(holder.binding.checkedText, ColorStateList.valueOf(Color.GRAY));
        } else {
            holder.binding.checkedText.setChecked(stat);
        }

        //绑定复选框的选择监听器
        holder.binding.checkedText.setOnClickListener(
                view -> {
                    holder.binding.checkedText.toggle();
                    itemStats[position] = !itemStats[position];
                    listener.onChecked(position, itemStats[position]);
                }
        );
    }

    @Override
    public int getItemCount() {
        return itemNames.length;
    }
}
