package com.manager.assistant.ui.pages.bookkeeping.budget;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.manager.assistant.data.classes.Budget;
import com.manager.assistant.data.controllers.BudgetDataController;
import com.manager.assistant.databinding.ViewHolderBudgetBinding;
import com.manager.assistant.helpers.ExceptionHelper;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
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

    public interface ViewHolderListener {
        /**
         * 当ViewHolder被点击时的监听器
         *
         * @param position 被点击的ViewHolder在Adapter中的真实下标
         */
        void onClicked(int position);

        /**
         * 重置按钮点击监听
         *
         * @param position 被点击的ViewHolder在Adapter中的真实下标
         * @param context  上下文
         */
        void onReset(int position, Context context);
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        ViewHolderBudgetBinding binding;

        public BudgetViewHolder(@NonNull ViewHolderBudgetBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置触摸动画
            AppearanceAnimationHelper.attachMorphAnimation(binding.getRoot());

            //设置点击监听
            binding.getRoot().setOnClickListener(v -> listener.onClicked(getBindingAdapterPosition()));
            binding.resetBtn.setOnClickListener(v -> listener.onReset(
                    getBindingAdapterPosition(),
                    binding.getRoot().getContext()
            ));
        }
    }

    public BudgetAdapter(OnBudgetClickedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderBudgetBinding binding = ViewHolderBudgetBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new BudgetViewHolder(binding, new ViewHolderListener() {
            @Override
            public void onClicked(int position) {
                Budget budget = budgetList.get(position);
                listener.onClicked(budget, position);
            }

            @Override
            public void onReset(int position, Context context) {
                Budget budget = budgetList.get(position);
                new MaterialAlertDialogBuilder(context)
                        .setTitle("重置预算")
                        .setMessage("此操作将重置预算的余额并将起算日期设置为今天，确认继续吗？")
                        .setPositiveButton("确定", (dialog, which) -> {
                            try {
                                BudgetDataController.resetBudget(budget.getBno(), context);
                                Toast.makeText(context, String.format(Locale.getDefault(), "%s已成功重置", budget.getName()), Toast.LENGTH_SHORT).show();
                            } catch (SQLiteException e) {
                                Toast.makeText(context, "预算重置失败", Toast.LENGTH_SHORT).show();
                                ExceptionHelper.showExceptionDialog(context, e);
                                return;
                            }

                            onResetDialogConfirmed(position);
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
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
        holder.binding.nameText.setText(name);
        holder.binding.startDateText.setText(startDate);
        String amountStr = String.format(Locale.getDefault(), "%.2f/%.2f", leftAmount, initAmount);
        holder.binding.amountText.setText(amountStr);
        holder.binding.resetFrequencyText.setText(resetFrequency.getTitle());

        //设置圆角
        AppearanceAnimationHelper.setRecyclerItemRadius(holder.itemView, budgetList.size(), position);

        //设置点击监听
        holder.itemView.setOnClickListener(v -> listener.onClicked(budget, position));

        //设置重置按钮点击监听
        Context context = holder.itemView.getContext();
        holder.binding.resetBtn.setOnClickListener(v -> new MaterialAlertDialogBuilder(context)
                .setTitle("重置预算")
                .setMessage("此操作将重置预算的余额并将起算日期设置为今天，确认继续吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    try {
                        BudgetDataController.resetBudget(budget.getBno(), context);
                        Toast.makeText(context, String.format(Locale.getDefault(), "%s已成功重置", budget.getName()), Toast.LENGTH_SHORT).show();
                    } catch (SQLiteException e) {
                        Toast.makeText(context, "预算重置失败", Toast.LENGTH_SHORT).show();
                        ExceptionHelper.showExceptionDialog(context, e);
                        return;
                    }

                    onResetDialogConfirmed(position);
                })
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
     */
    private void onResetDialogConfirmed(int position) {
        Budget budget = budgetList.get(position);
        budget.reset();
        notifyItemChanged(position);
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
     */
    public void addBudget(@NonNull Bundle dataBundle) {
        long bno = dataBundle.getLong(KeyValueStrings.BNO.getValue());                                      //预算编号
        String name = dataBundle.getString(KeyValueStrings.BUDGET_NAME.getValue());                         //预算名称
        double initAmount = dataBundle.getDouble(KeyValueStrings.INIT_AMOUNT.getValue());                   //初始金额
        String startDate = dataBundle.getString(KeyValueStrings.START_DATE.getValue());                     //起算日期
        String resetFrequencyStr = dataBundle.getString(KeyValueStrings.BUDGET_RESET_FREQUENCY.getValue()); //重置频率
        ResetFrequency resetFrequency = ResetFrequency.valueOf(resetFrequencyStr);
        long[] tagNos = dataBundle.getLongArray(KeyValueStrings.TAG_NO.getValue());                         //预算标签
        if (tagNos == null) return;
        List<Long> tagNoList = Arrays.stream(tagNos)
                .boxed()
                .collect(Collectors.toList());

        //更新 UI
        Budget budget = new Budget(bno, name, initAmount, startDate, resetFrequency, tagNoList);
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
     */
    public void modifyBudget(@NonNull Bundle dataBundle) {
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

        //更新 UI
        int position = dataBundle.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue());
        Budget budget = new Budget(bno, name, initAmount, leftAmount, startDate, resetFrequency, tagNoList);
        budgetList.set(position, budget);
        notifyItemChanged(position);
        BudgetRepository repository = BudgetRepository.getInstance();
        repository.onUpdated(budget, BudgetUpdateReason.MODIFIED);
    }

    /**
     * 删除预算
     *
     * @param dataBundle 待删除的预算的数据包
     */
    public void deleteBudget(@NonNull Bundle dataBundle) {
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
