package com.project.manager.data.data_class.running_account;

import androidx.annotation.NonNull;

import com.project.manager.ui.pages.bookkeeping.running_account_edit.fragments.RunningAccountType; /**
 * 收入流水类
 */
public class IncomeRunningAccount extends RunningAccountBase {
    protected final String default_remark = "一条收入记录";

    /**
     * 不给定编号的构造方法
     *
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     */
    public IncomeRunningAccount(@NonNull String remark, String date_time, double amount, boolean isDefaultRemark) {
        this.rno = -1;
        this.type = RunningAccountType.INCOME;
        this.name = "收入";
        this.remark = remark.isEmpty() ? default_remark : remark;
        this.date_time = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
    }

    /**
     * 给定编号的构造方法
     *
     * @param rno       编号
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     */
    public IncomeRunningAccount(long rno, @NonNull String remark, String date_time, double amount, boolean isDefaultRemark) {
        this.rno = rno;
        this.type = RunningAccountType.INCOME;
        this.name = "收入";
        this.remark = remark.isEmpty() ? default_remark : remark;
        this.date_time = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
    }
}
