package com.project.manager.ui.setting.data_io;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.project.manager.R;

public class MultiChoiceDialogAdapter extends RecyclerView.Adapter<MultiChoiceDialogAdapter.CheckedTextViewHolder> {
    private final String[] choiceItems;         //多选选项
    private final boolean[] itemStats;          //选项初始状态
    private final boolean[] itemEnabled;        //选项是否被禁用
    private final onCheckedListener listener;   //选择行为监听器

    public static class CheckedTextViewHolder extends RecyclerView.ViewHolder {
        AppCompatCheckedTextView checkedTextView;

        public CheckedTextViewHolder(View itemView) {
            super(itemView);
            checkedTextView = itemView.findViewById(R.id.checked_text);
        }
    }

    public interface onCheckedListener {
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
     * @param itemStats   选项初始状态
     * @param choiceItems 选项名称
     * @param listener    选项点击监听器
     */
    public MultiChoiceDialogAdapter(boolean[] itemStats, String[] choiceItems, onCheckedListener listener) {
        this.itemEnabled = null;
        this.itemStats = itemStats;
        this.choiceItems = choiceItems;
        this.listener = listener;
    }

    /**
     * 不设置是否启用可选项的构造方法
     *
     * @param itemStats   选项初始状态
     * @param itemEnabled 选项是否启用
     * @param choiceItems 选项名称
     * @param listener    选项点击监听器
     */
    public MultiChoiceDialogAdapter(boolean[] itemEnabled, boolean[] itemStats, String[] choiceItems, onCheckedListener listener) {
        this.itemEnabled = itemEnabled;
        this.itemStats = itemStats;
        this.choiceItems = choiceItems;
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

        holder.checkedTextView.setText(choiceItems[position]);
        if (!isEnabled) {
            holder.checkedTextView.setEnabled(false);
            holder.checkedTextView.setChecked(false);
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
        return choiceItems.length;
    }
}
