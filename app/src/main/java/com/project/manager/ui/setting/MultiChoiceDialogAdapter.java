package com.project.manager.ui.setting;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

public class MultiChoiceDialogAdapter extends RecyclerView.Adapter<MultiChoiceDialogAdapter.CheckBoxViewHolder> {
    private final String[] choiceItems;         //多选选项
    private final boolean[] itemStats;          //选项初始状态
    private final boolean[] itemEnabled;        //选项是否被禁用
    private final Context context;              //上下文
    private final onCheckedListener listener;   //选择行为监听器

    public static class CheckBoxViewHolder extends RecyclerView.ViewHolder {
        MaterialCheckBox checkBox;

        public CheckBoxViewHolder(MaterialCheckBox checkBox) {
            super(checkBox);
            this.checkBox = checkBox;
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
     * @param context     上下文
     * @param itemStats   选项初始状态
     * @param choiceItems 选项名称
     * @param listener    选项点击监听器
     */
    public MultiChoiceDialogAdapter(Context context, boolean[] itemStats, String[] choiceItems, onCheckedListener listener) {
        this.context = context;
        this.itemEnabled = null;
        this.itemStats = itemStats;
        this.choiceItems = choiceItems;
        this.listener = listener;
    }

    /**
     * 不设置是否启用可选项的构造方法
     *
     * @param context     上下文
     * @param itemStats   选项初始状态
     * @param itemEnabled 选项是否启用
     * @param choiceItems 选项名称
     * @param listener    选项点击监听器
     */
    public MultiChoiceDialogAdapter(Context context, boolean[] itemEnabled, boolean[] itemStats, String[] choiceItems, onCheckedListener listener) {
        this.context = context;
        this.itemEnabled = itemEnabled;
        this.itemStats = itemStats;
        this.choiceItems = choiceItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CheckBoxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MaterialCheckBox checkBox = new MaterialCheckBox(context);
        return new CheckBoxViewHolder(checkBox);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckBoxViewHolder holder, int position) {
        boolean isEnabled = true;
        if (itemEnabled != null) {
            isEnabled = itemEnabled[position];
        }
        boolean stat = itemStats[position];

        holder.checkBox.setText(choiceItems[position]);
        if (!isEnabled) {
            holder.checkBox.setEnabled(false);
            holder.checkBox.setChecked(false);
        } else {
            holder.checkBox.setChecked(stat);
        }

        //绑定复选框的选择监听器
        holder.checkBox.setOnCheckedChangeListener(
                (buttonView, isChecked) -> listener.onChecked(position, isChecked)
        );
    }

    @Override
    public int getItemCount() {
        return choiceItems.length;
    }
}
