package com.manager.assistant.ui.others.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.R;
import com.manager.assistant.helpers.appearence.TextViewFlagHelper;

public class MultiChoiceDialogAdapter extends RecyclerView.Adapter<MultiChoiceDialogAdapter.CheckedTextViewHolder> {
    private final String[] itemNames;         //多选选项
    private final boolean[] itemStats;          //选项初始状态
    private final boolean[] itemEnabled;        //选项是否被禁用
    private final OnCheckedListener listener;   //选择行为监听器

    public static class CheckedTextViewHolder extends RecyclerView.ViewHolder {
        AppCompatCheckedTextView checkedTextView;

        public CheckedTextViewHolder(View itemView) {
            super(itemView);
            checkedTextView = itemView.findViewById(R.id.checked_text);
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
    public CheckedTextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_multichoice_item, parent, false);
        return new CheckedTextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckedTextViewHolder holder, int position) {
        boolean isEnabled = true;
        if (itemEnabled != null) {
            isEnabled = itemEnabled[position];
        }
        boolean stat = itemStats[position];

        //设置选项的可用性
        holder.checkedTextView.setText(itemNames[position]);
        if (!isEnabled) {
            holder.checkedTextView.setEnabled(false);
            holder.checkedTextView.setChecked(false);

            TextViewFlagHelper.setDeleteLine(holder.checkedTextView, true);
        } else {
            holder.checkedTextView.setChecked(stat);
        }

        //绑定复选框的选择监听器
        holder.checkedTextView.setOnClickListener(
                view -> {
                    holder.checkedTextView.toggle();
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
