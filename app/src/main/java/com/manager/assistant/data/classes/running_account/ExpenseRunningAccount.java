package com.manager.assistant.data.classes.running_account;

import androidx.annotation.NonNull;

import com.manager.assistant.ui.pages.main.bookkeeping.fragments.RunningAccountType;

public class ExpenseRunningAccount extends RunningAccountBase {
    /**
     * 不给定编号的构造方法
     *
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     */
    public ExpenseRunningAccount(@NonNull String remark, String date_time, double amount) {
        super();
        this.type = RunningAccountType.EXPENSE;
        this.title = "支出";
        this.remark = remark;
        this.datetime = date_time;
        this.amount = amount;
        this.rno = -1;
    }

    /**
     * 给定编号的构造方法（用于数据库查询时）
     *
     * @param rno       编号
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     */
    public ExpenseRunningAccount(long rno, @NonNull String remark, String date_time, double amount) {
        super();
        this.rno = rno;
        this.type = RunningAccountType.EXPENSE;
        this.title = "支出";
        this.remark = remark;
        this.datetime = date_time;
        this.amount = amount;
    }

    @Override
    protected String initDefaultRemark() {
        return "一条支出记录";
    }
}
