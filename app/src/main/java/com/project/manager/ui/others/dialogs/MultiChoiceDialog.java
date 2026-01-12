package com.project.manager.ui.others.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.project.manager.R;

/**
 * 可禁用选项的多选对话框
 */
public class MultiChoiceDialog extends CustomDialogBase {
    private final MultiChoiceDialogAdapter.OnCheckedListener checkedListener; //选项选择监听器
    private final boolean[] initStats;                                        //选项初始状态
    private final String[] itemNames;                                         //选项名称
    private final boolean[] itemsEnabled;                                     //选项启用状态

    /**
     * 允许禁用选项的构造方法
     *
     * @param context         上下文
     * @param dialogTitle     对话框标题
     * @param itemsEnabled    是否启用对应选项
     * @param initStats       选项初始状态
     * @param itemNames       选项名称
     * @param checkedListener 选项选择监听器
     */
    public MultiChoiceDialog(
            Context context,
            String dialogTitle,
            boolean[] itemsEnabled,
            boolean[] initStats,
            String[] itemNames,
            MultiChoiceDialogAdapter.OnCheckedListener checkedListener) {
        super(context, dialogTitle);

        this.initStats = initStats;
        this.itemNames = itemNames;
        this.itemsEnabled = itemsEnabled;
        this.checkedListener = checkedListener;
    }

    /**
     * 不禁用任何选项的构造方法
     *
     * @param context         上下文
     * @param dialogTitle     对话框标题
     * @param initStats       选项初始状态
     * @param itemNames       选项名称
     * @param checkedListener 选项选择监听器
     */
    public MultiChoiceDialog(
            Context context,
            String dialogTitle,
            boolean[] initStats,
            String[] itemNames,
            MultiChoiceDialogAdapter.OnCheckedListener checkedListener) {
        this(context, dialogTitle, null, initStats, itemNames, checkedListener);
    }

    @Override
    protected View getView() {
        @SuppressLint("InflateParams") View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_multichoice, null);

        //多选选项列表
        RecyclerView itemListView = view.findViewById(R.id.item_recycler);
        MultiChoiceDialogAdapter adapter = new MultiChoiceDialogAdapter(itemsEnabled, initStats, itemNames, checkedListener);
        itemListView.setAdapter(adapter);

        return view;
    }
}
