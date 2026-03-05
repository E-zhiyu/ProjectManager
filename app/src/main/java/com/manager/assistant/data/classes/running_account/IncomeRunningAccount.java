package com.manager.assistant.data.classes.running_account;

import androidx.annotation.NonNull;

import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

public class IncomeRunningAccount extends RunningAccountBase {
    /**
     * 不给定编号的构造方法
     *
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     */
    public IncomeRunningAccount(@NonNull String remark, String date_time, double amount) {
        super();
        this.rno = -1;
        this.type = RunningAccountType.INCOME;
        this.title = "收入";
        this.remark = remark;
        this.datetime = date_time;
        this.amount = amount;
    }

    /**
     * 给定编号的构造方法
     *
     * @param rno       编号
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     */
    public IncomeRunningAccount(long rno, @NonNull String remark, String date_time, double amount) {
        super();
        this.rno = rno;
        this.type = RunningAccountType.INCOME;
        this.title = "收入";
        this.remark = remark;
        this.datetime = date_time;
        this.amount = amount;
    }

    @Override
    protected String initDefaultRemark() {
        return "一条收入记录";
    }
}
