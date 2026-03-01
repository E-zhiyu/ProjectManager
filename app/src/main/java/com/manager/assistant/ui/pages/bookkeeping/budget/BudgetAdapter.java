package com.manager.assistant.ui.pages.bookkeeping.budget;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.shape.Shapeable;
import com.google.android.material.textview.MaterialTextView;
import com.manager.assistant.R;
import com.manager.assistant.data.data_class.Budget;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.isolated_enums.KeyValueStrings;
import com.manager.assistant.ui.others.listeners.SpringAnimationOnTouchListener;

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
        SpringAnimationOnTouchListener onTouchListener;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.name_text);
            startDateText = itemView.findViewById(R.id.start_date_text);
            amountText = itemView.findViewById(R.id.amount_text);
            resetFrequencyText = itemView.findViewById(R.id.reset_frequency_text);

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

        holder.nameText.setText(name);
        holder.startDateText.setText(startDate);
        String amountStr = String.format(Locale.getDefault(), "%.2f/%.2f", leftAmount, initAmount);
        holder.amountText.setText(amountStr);
        holder.resetFrequencyText.setText(resetFrequency.getTitle());

        holder.itemView.setOnClickListener(v -> listener.onClicked(budget, position));
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
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
            long bno = Budget.saveNewBudget(budget, context);
            budget.setBno(bno);
        } catch (SQLException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        //更新UI
        budgetList.add(budget);
        notifyItemInserted(budgetList.size() - 1);
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
            Budget.modifyBudget(budget, context);
        } catch (SQLException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        //更新UI
        int position = dataBundle.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());
        budgetList.set(position, budget);
        notifyItemChanged(position);
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
            Budget.deleteBudget(bno, context);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        //更新UI
        int position = dataBundle.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());
        budgetList.remove(position);
        notifyItemRemoved(position);
    }
}
