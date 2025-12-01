package com.project.manager.ui.bookkeeping.running_account_edit;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.project.manager.R;
import com.project.manager.helpers.ExceptionHelper;
import com.project.manager.ui.bookkeeping.KeyValueStrings;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;

import java.util.List;

public class RunningAccountRecyclerAdapter extends RecyclerView.Adapter<RunningAccountRecyclerAdapter.RunningAccountViewHolder> {
    private final List<RunningAccountBase> runningAccountList;   //数据源
    private final Context context;                              //上下文
    private final OnRunningAccountViewClickListener listener;   //单击接口对象

    public static class RunningAccountViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView amount_text, remark_text, name_datetime_text;

        public RunningAccountViewHolder(@NonNull View itemView) {
            super(itemView);
            amount_text = itemView.findViewById(R.id.amount_text);
            remark_text = itemView.findViewById(R.id.remark_textview);
            name_datetime_text = itemView.findViewById(R.id.name_datetime_textview);
        }
    }

    //定义流水视图点击事件接口
    public interface OnRunningAccountViewClickListener {
        void onRunningAccountViewClick(int position, RunningAccountBase runningAccountBase);
    }

    /**
     * 构造方法
     *
     * @param runningAccountList 流水数据类型列表
     */
    public RunningAccountRecyclerAdapter(List<RunningAccountBase> runningAccountList, OnRunningAccountViewClickListener listener, Context context) {
        this.runningAccountList = runningAccountList;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public RunningAccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_running_account, parent, false);
        return new RunningAccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RunningAccountViewHolder holder, int position) {
        RunningAccountBase currentRunningAccount = runningAccountList.get(position);
        String name_and_datetime = String.format("%s·%s",
                currentRunningAccount.getName(),
                currentRunningAccount.getDate_time()
        );

        holder.remark_text.setText(currentRunningAccount.getRemark());                  //备注
        holder.name_datetime_text.setText(name_and_datetime);                           //名称和日期
        holder.amount_text.setText(String.valueOf(currentRunningAccount.getAmount()));  //金额

        holder.itemView.setOnClickListener(v -> listener.onRunningAccountViewClick(position, currentRunningAccount));
    }

    @Override
    public int getItemCount() {
        return this.runningAccountList.size();
    }

    /**
     * 获取指定位置的流水数据类型
     *
     * @param position 指定的下标
     * @return 流水数据类型
     */
    public RunningAccountBase getItem(int position) {
        return runningAccountList.get(position);
    }

    /**
     * 添加新流水视图
     *
     * @param dataBundle 新建流水的数据包
     */
    public void addNewRunningAccount(@NonNull Bundle dataBundle) {
        //将流水保存至数据库
        long rno;
        try {
            rno = RunningAccountBase.saveNewAccount(dataBundle, context);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, "添加流水记录时出错", Toast.LENGTH_SHORT).show();
            return;
        }

        //获取基本流水数据
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        boolean isDefaultRemark = dataBundle.getBoolean(KeyValueStrings.ACCOUNT_IS_DEFAULT_REMARK.getValue());
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());

        //获取特殊数据并实例化流水类
        RunningAccountBase newRunningAccount;
        if (type == RunningAccountType.EXPENSE) {
            newRunningAccount = new ExpenseRunningAccount(remark, date_time, amount, isDefaultRemark);
        } else if (type == RunningAccountType.INCOME) {
            newRunningAccount = new IncomeRunningAccount(remark, date_time, amount, isDefaultRemark);
        } else if (type == RunningAccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());    //转出账户
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());    //转入账户
            newRunningAccount = new TransferRunningAccount(remark, date_time, amount, isDefaultRemark, exportAccount, importAccount);
        } else {
            NullPointerException e = new NullPointerException("流水类型获取失败");
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        newRunningAccount.setRno(rno);  //保存流水编号

        this.runningAccountList.add(0, newRunningAccount);
        notifyItemInserted(0);
    }

    /**
     * 修改指定下标的流水视图
     *
     * @param dataBundle 修改后的流水数据
     */
    public void modifyRunningAccountView(@NonNull Bundle dataBundle) {
        //将数据保存至数据库
        try {
            RunningAccountBase.modifyAccount(dataBundle, context);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, "修改流水数据失败", Toast.LENGTH_SHORT).show();
            return;
        }

        //解析数据
        long rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        int position = dataBundle.getInt(KeyValueStrings.VIEW_HOLDER_POSITION.getValue(), -1);    //原视图下标
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        boolean isDefaultRemark = dataBundle.getBoolean(KeyValueStrings.ACCOUNT_IS_DEFAULT_REMARK.getValue());
        String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());

        //实例化流水类
        RunningAccountBase runningAccount;
        if (type == RunningAccountType.EXPENSE) {
            runningAccount = new ExpenseRunningAccount(remark, date_time, amount, isDefaultRemark);
        } else if (type == RunningAccountType.INCOME) {
            runningAccount = new IncomeRunningAccount(remark, date_time, amount, isDefaultRemark);
        } else if (type == RunningAccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());    //转出账户
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());    //转入账户
            runningAccount = new TransferRunningAccount(remark, date_time, amount, isDefaultRemark, exportAccount, importAccount);
        } else {
            NullPointerException e = new NullPointerException("流水类型获取失败");
            ExceptionHelper.showExceptionDialog(context, e);
            return;
        }

        runningAccount.setRno(rno);

        this.runningAccountList.set(position, runningAccount);
        notifyItemChanged(position);
    }

    /**
     * 删除指定下标的流水记录
     *
     * @param position 待删除的流水记录的下标
     */
    public void deleteRunningAccountView(int position) {
        if (position == -1) return;

        //获取待删除的流水数据
        RunningAccountBase runningAccount = runningAccountList.get(position);
        RunningAccountType type = runningAccount.getType();
        long rno = runningAccount.getRno();

        //从数据库中删除
        try {
            RunningAccountBase.deleteAccount(rno, type, context);
        } catch (SQLiteException e) {
            ExceptionHelper.showExceptionDialog(context, e);
            Toast.makeText(context, "流水记录删除失败", Toast.LENGTH_SHORT).show();
            return;
        }

        //刷新UI
        runningAccountList.remove(position);
        notifyItemRemoved(position);
    }
}
