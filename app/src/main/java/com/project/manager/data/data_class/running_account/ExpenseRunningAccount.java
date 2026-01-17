package com.project.manager.data.data_class.running_account;

import androidx.annotation.NonNull;

import com.project.manager.ui.pages.bookkeeping.running_account.fragments.RunningAccountType; /**
 * 支出流水类
 */
public class ExpenseRunningAccount extends RunningAccountBase {
    protected final String default_remark = "一条支出记录";

    /**
     * 不给定编号的构造方法
     *
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     */
    public ExpenseRunningAccount(@NonNull String remark, String date_time, double amount, boolean isDefaultRemark) {
        this.type = RunningAccountType.EXPENSE;
        this.name = "支出";
        this.remark = remark.isEmpty() ? default_remark : remark;
        this.isDefaultRemark = remark.isEmpty();
        this.datetime = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
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
    public ExpenseRunningAccount(long rno, @NonNull String remark, String date_time, double amount, boolean isDefaultRemark) {
        this.rno = rno;
        this.type = RunningAccountType.EXPENSE;
        this.name = "支出";
        this.remark = remark.isEmpty() ? default_remark : remark;
        this.datetime = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
    }
}
