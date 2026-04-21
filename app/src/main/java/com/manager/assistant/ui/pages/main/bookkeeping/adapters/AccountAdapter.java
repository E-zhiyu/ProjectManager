package com.manager.assistant.ui.pages.main.bookkeeping.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manager.assistant.data.classes.running_account.ExpenseRunningAccount;
import com.manager.assistant.data.classes.running_account.IncomeRunningAccount;
import com.manager.assistant.data.classes.running_account.TransferRunningAccount;
import com.manager.assistant.databinding.ViewHolderRunningAccountBinding;
import com.manager.assistant.data.classes.running_account.RunningAccountBase;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.helpers.appearence.AppearanceAnimationHelper;
import com.manager.assistant.ui.pages.main.bookkeeping.fragments.RunningAccountType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.RunningAccountViewHolder> {
    private final List<RunningAccountBase> accountList = new ArrayList<>(); //数据源
    private final OnViewClickListener listener;                             //ViewHolder单击监听

    /**
     * 流水记录点击接口
     */
    public interface OnViewClickListener {
        void onRunningAccountClick(RunningAccountBase runningAccountBase);
    }

    public interface ViewHolderListener {
        /**
         * ViewHolder的点击监听
         *
         * @param position 悲点击的ViewHolder在适配器中的位置
         */
        void onClicked(int position);
    }

    public static class RunningAccountViewHolder extends RecyclerView.ViewHolder {
        ViewHolderRunningAccountBinding binding;

        public RunningAccountViewHolder(@NonNull ViewHolderRunningAccountBinding binding, ViewHolderListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            //设置点击监听
            binding.getRoot().setOnClickListener(view -> listener.onClicked(getBindingAdapterPosition()));

            //设置触摸监听
            AppearanceAnimationHelper.attachMorphAnimation(binding.getRoot());
        }
    }

    /**
     * 构造方法
     *
     * @param listener 流水记录点击监听
     */
    public AccountAdapter(List<RunningAccountBase> accountList, OnViewClickListener listener) {
        this.accountList.addAll(accountList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public RunningAccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolderRunningAccountBinding binding = ViewHolderRunningAccountBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new RunningAccountViewHolder(binding, position -> {
            RunningAccountBase runningAccount = accountList.get(position);
            listener.onRunningAccountClick(runningAccount);
        });
    }

    @Override
    public void onBindViewHolder(@NonNull RunningAccountViewHolder holder, int position) {
        RunningAccountBase runningAccount = accountList.get(position);

        //获取流水数据
        String type = runningAccount.getType().getTitle();
        String datetime = runningAccount.getDatetime();
        String typeAndDatetime = String.format(Locale.getDefault(), "%s·%s", type, datetime);
        String remark = runningAccount.getRemark();
        double amount = runningAccount.getAmount();

        //初始化文本视图
        holder.binding.amountText.setText(String.format(Locale.getDefault(), "%.2f", amount));
        holder.binding.remarkText.setText(remark.isEmpty() ? runningAccount.getDefaultRemark() : remark);
        holder.binding.typeDatetimeText.setText(typeAndDatetime);

        //设置圆角
        AppearanceAnimationHelper.setRecyclerItemRadius(holder.binding.getRoot(), accountList.size(), position);
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    /**
     * 添加流水记录
     *
     * @param dataBundle 流水记录数据包
     */
    public void addRunningAccount(@NonNull Bundle dataBundle) {
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String datetime = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        long rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue(), 0);
        if (rno == 0) return;   //如果为0则说明数据库保存失败，直接结束该方法

        //获取特殊数据并实例化流水类
        RunningAccountBase runningAccount;
        if (type == RunningAccountType.EXPENSE) {
            runningAccount = new ExpenseRunningAccount(remark, datetime, amount);
        } else if (type == RunningAccountType.INCOME) {
            runningAccount = new IncomeRunningAccount(remark, datetime, amount);
        } else if (type == RunningAccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());    //转出账户
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());    //转入账户
            runningAccount = new TransferRunningAccount(remark, datetime, amount, exportAccount, importAccount);
        } else {
            return;
        }
        runningAccount.setRno(rno);

        //添加到视图中
        accountList.add(0, runningAccount);
        notifyItemInserted(0);
        notifyItemChanged(1);   //更新圆角
    }

    /**
     * 修改流水记录
     *
     * @param dataBundle 修改后的流水记录数据
     */
    public void modifyRunningAccount(@NonNull Bundle dataBundle) {
        //解析数据
        long rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        String datetime = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());

        //实例化流水类
        RunningAccountBase runningAccount;
        if (type == RunningAccountType.EXPENSE) {
            runningAccount = new ExpenseRunningAccount(remark, datetime, amount);
        } else if (type == RunningAccountType.INCOME) {
            runningAccount = new IncomeRunningAccount(remark, datetime, amount);
        } else if (type == RunningAccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());    //转出账户
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());    //转入账户
            runningAccount = new TransferRunningAccount(remark, datetime, amount, exportAccount, importAccount);
        } else {
            return;
        }
        runningAccount.setRno(rno);

        //更新UI
        int index = 0;
        for (RunningAccountBase base : accountList) {
            if (base.getRno() == rno) {
                accountList.set(index, runningAccount);
                notifyItemChanged(index);
                break;
            }
            index++;
        }
    }

    /**
     * 删除流水记录
     *
     * @param rno 需要删除的流水记录的编号
     */
    public void deleteRunningAccount(long rno) {
        int index = 0;
        for (RunningAccountBase base : accountList) {
            if (base.getRno() == rno) {
                accountList.remove(index);
                notifyItemRemoved(index);
                notifyItemChanged(accountList.size() - 1);  //更新圆角
                break;
            }
            index++;
        }
    }
}
