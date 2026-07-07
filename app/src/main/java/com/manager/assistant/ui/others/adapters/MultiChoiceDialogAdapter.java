package com.manager.assistant.ui.others.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;
import com.manager.assistant.R;
import com.manager.assistant.databinding.ViewHolderMultichoiceItemBinding;
import com.manager.assistant.ui.others.dialogs.MultiChoiceDialogBuilder;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MultiChoiceDialogAdapter extends RecyclerView.Adapter<MultiChoiceDialogAdapter.MultiChoiceItemViewHolder> {
    private final List<MultiChoiceDialogBuilder.ChoiceItem> itemList;  //选项列表
    private final List<Boolean> checkedStatList;                //选项选择状态列表，与实际 UI 应保持一致

    public static class MultiChoiceItemViewHolder extends RecyclerView.ViewHolder {
        ViewHolderMultichoiceItemBinding binding;

        public MultiChoiceItemViewHolder(@NonNull ViewHolderMultichoiceItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    /**
     * 不设置是否启用可选项的构造方法
     *
     * @param itemList 选项列表
     */
    public MultiChoiceDialogAdapter(@NonNull List<MultiChoiceDialogBuilder.ChoiceItem> itemList) {
        this.itemList = itemList;
        this.checkedStatList = itemList.stream()
                .map(MultiChoiceDialogBuilder.ChoiceItem::getInitStat)
                .collect(Collectors.toList());
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
        MultiChoiceDialogBuilder.ChoiceItem item = itemList.get(position);
        Context context = holder.itemView.getContext();
        boolean itemEnabled = item.isEnabled();
        boolean stat = item.getInitStat();
        String itemTitle = item.getTitle();

        //设置选项的可用性
        holder.binding.checkedText.setText(itemTitle);
        if (!itemEnabled) {
            //禁用选项
            holder.binding.checkedText.setEnabled(false);
            holder.binding.checkedText.setChecked(false);

            //文字和图标变为灰色
            int disabledColor = MaterialColors.getColor(
                    context,
                    com.google.android.material.R.attr.colorOutline,
                    Color.GRAY
            );
            holder.binding.checkedText.setTextColor(disabledColor);
            TextViewCompat.setCompoundDrawableTintList(holder.binding.checkedText, ColorStateList.valueOf(disabledColor));

            //加上禁用提示字样
            String notIncluded = String.format(
                    Locale.getDefault(),
                    "%s(%s)",
                    holder.binding.checkedText.getText(),
                    context.getString(R.string.not_included)
            );
            holder.binding.checkedText.setText(notIncluded);
        } else {
            holder.binding.checkedText.setChecked(stat);
        }

        //绑定复选框的选择监听器
        holder.binding.checkedText.setOnClickListener(
                view -> {
                    holder.binding.checkedText.toggle();    //更新 UI
                    checkedStatList.set(position, holder.binding.checkedText.isChecked());  //更新选中状态
                }
        );
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public List<Boolean> getCheckedStatList() {
        return checkedStatList;
    }
}
