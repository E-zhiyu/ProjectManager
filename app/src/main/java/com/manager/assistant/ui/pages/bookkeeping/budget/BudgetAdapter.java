package com.manager.assistant.ui.pages.bookkeeping.budget;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.Shapeable;
import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.data.classes.Budget;
import com.manager.assistant.data.controllers.BudgetDataController;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
import com.manager.assistant.ui.others.listeners.SpringAnimationOnTouchListener;
import com.manager.assistant.ui.sync.budget.BudgetRepository;
import com.manager.assistant.ui.sync.budget.BudgetUpdateReason;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private final List<Budget> budgetList = new ArrayList<>();  //预算列表
    private final OnBudgetClickedListener listener;             //ViewHolder点击监听器

    public interface OnBudgetClickedListener {
        void onClicked(Budget budget, int position);
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView nameText, startDateText, amountText, resetFrequencyText;
        ImageButton resetBtn;
        SpringAnimationOnTouchListener onTouchListener;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.name_text);
            startDateText = itemView.findViewById(R.id.start_date_text);
            amountText = itemView.findViewById(R.id.amount_text);
            resetFrequencyText = itemView.findViewById(R.id.reset_frequency_text);
            resetBtn = itemView.findViewById(R.id.reset_btn);

            //设置触摸监听器
            Shapeable shapeable = (Shapeable) itemView;
            Vibrator vibrator = (Vibrator) itemView.getContext()
                    .getSystemService(Context.VIBRATOR_SERVICE);
            onTouchListener = new SpringAnimationOnTouchListener(shapeable, vibrator);
            itemView.setOnTouchListener(onTouchListener);
        }
    }

    public BudgetAdapter(OnBudgetClickedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);
        String name = budget.getName();
        String startDate = budget.getStartDate();
        double initAmount = budget.getInitAmount();
        double leftAmount = budget.getLeftAmount();
        ResetFrequency resetFrequency = budget.getResetFrequency();

        //填充各种信息文本
        holder.nameText.setText(name);
        holder.startDateText.setText(startDate);
        String amountStr = String.format(Locale.getDefault(), "%.2f/%.2f", leftAmount, initAmount);
        holder.amountText.setText(amountStr);
        holder.resetFrequencyText.setText(resetFrequency.getTitle());

        //设置圆角
        AppearanceAnimationHelper.setRecyclerItemRadius(holder.itemView, budgetList.size(), position);

        //设置点击监听
        holder.itemView.setOnClickListener(v -> listener.onClicked(budget, position));

        //设置重置按钮点击监听
        Context context = holder.itemView.getContext();
        holder.resetBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(context)
                .setTitle("重置预算")
                .setMessage("此操作将重置预算的余额并将起算日期设置为今天，确认继续吗？")
                .setPositiveButton("确定", (dialog, which) -> onResetDialogConfirmed(position, context))
                .setNegativeButton("取消", null)
                .show());
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    /**
     * 确认重置回调
     *
     * @param position 重置的预算所在的下标
     * @param context  上下文
     */
    private void onResetDialogConfirmed(int position, Context context) {
        Budget budget = budgetList.get(position);
        try {
            BudgetDataController.resetBudget(budget.getBno(), context);
            budget.reset();
            notifyItemChanged(position);
            Toast.makeText(context, String.format(Locale.getDefault(), "%s已成功重置", budget.getName()), Toast.LENGTH_SHORT).show();
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
        }
    }

    /**
     * 刷新预算列表
     *
     * @param budgetList 新预算列表数据
     */
    public void refreshBudget(List<Budget> budgetList) {
        int oldCount = this.budgetList.size();
        this.budgetList.clear();
        notifyItemRangeRemoved(0, oldCount);

        this.budgetList.addAll(budgetList);
        notifyItemRangeInserted(0, budgetList.size());
    }

    /**
     * 添加新预算
     *
     * @param dataBundle 新预算的数据包
     * @param context    上下文
     */
    public void addBudget(@NonNull Bundle dataBundle, Context context) {
        String name = dataBundle.getString(KeyValueStrings.BUDGET_NAME.getValue());
        double initAmount = dataBundle.getDouble(KeyValueStrings.INIT_AMOUNT.getValue());
        String startDate = dataBundle.getString(KeyValueStrings.START_DATE.getValue());
        String resetFrequencyStr = dataBundle.getString(KeyValueStrings.BUDGET_RESET_FREQUENCY.getValue());
        ResetFrequency resetFrequency = ResetFrequency.valueOf(resetFrequencyStr);
        long[] tagNos = dataBundle.getLongArray(KeyValueStrings.TAG_NO.getValue());
        if (tagNos == null) return;
        List<Long> tagNoList = Arrays.stream(tagNos)
                .boxed()
                .collect(Collectors.toList());

        //保存至数据库
        Budget budget = new Budget(name, initAmount, startDate, resetFrequency, tagNoList);
        try {
            long bno = BudgetDataController.saveNewBudget(budget, context);
            budget.setBno(bno);
        } catch (SQLException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        //更新 UI
        budgetList.add(budget);
        notifyItemInserted(budgetList.size() - 1);
        notifyItemChanged(budgetList.size() - 2);   //更新尾部卡片圆角
        BudgetRepository repository = BudgetRepository.getInstance();
        repository.onUpdated(budget, BudgetUpdateReason.ADD);
    }

    /**
     * 修改预算
     *
     * @param dataBundle 修改后的预算的数据包
     * @param context    上下文
     */
    public void modifyBudget(@NonNull Bundle dataBundle, Context context) {
        long bno = dataBundle.getLong(KeyValueStrings.BNO.getValue());
        String name = dataBundle.getString(KeyValueStrings.BUDGET_NAME.getValue());
        double initAmount = dataBundle.getDouble(KeyValueStrings.INIT_AMOUNT.getValue());
        double leftAmount = dataBundle.getDouble(KeyValueStrings.LEFT_AMOUNT.getValue());
        String startDate = dataBundle.getString(KeyValueStrings.START_DATE.getValue());
        String resetFrequencyStr = dataBundle.getString(KeyValueStrings.BUDGET_RESET_FREQUENCY.getValue());
        ResetFrequency resetFrequency = ResetFrequency.valueOf(resetFrequencyStr);
        long[] tagNos = dataBundle.getLongArray(KeyValueStrings.TAG_NO.getValue());
        if (tagNos == null) return;
        List<Long> tagNoList = Arrays.stream(tagNos)
                .boxed()
                .collect(Collectors.toList());

        //写入数据
        Budget budget = new Budget(bno, name, initAmount, leftAmount, startDate, resetFrequency, tagNoList);
        try {
            BudgetDataController.modifyBudget(budget, context);
        } catch (SQLException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        //更新 UI
        int position = dataBundle.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());
        budgetList.set(position, budget);
        notifyItemChanged(position);
        BudgetRepository repository = BudgetRepository.getInstance();
        repository.onUpdated(budget, BudgetUpdateReason.MODIFIED);
    }

    /**
     * 删除预算
     *
     * @param dataBundle 待删除的预算的数据包
     * @param context    上下文
     */
    public void deleteBudget(@NonNull Bundle dataBundle, Context context) {
        long bno = dataBundle.getLong(KeyValueStrings.BNO.getValue());
        try {
            BudgetDataController.deleteBudget(bno, context);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        //更新 UI
        int position = dataBundle.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());
        budgetList.remove(position);
        notifyItemRemoved(position);
        BudgetRepository repository = BudgetRepository.getInstance();
        repository.onUpdated(BudgetUpdateReason.DELETED);

        //更新首尾卡片圆角
        if (position == budgetList.size()) {
            notifyItemChanged(budgetList.size() - 1);
        } else if (position == 0) {
            notifyItemChanged(0);
        }
    }
}
