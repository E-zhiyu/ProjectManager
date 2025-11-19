package com.project.manager.ui.bookkeeping;

import androidx.annotation.NonNull;

import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;

public abstract class RunningAccountBase {
    protected String name;              //名称
    protected RunningAccountType type;  //种类
    protected String remark;            //备注
    protected boolean isDefaultRemark;  //是否使用默认备注
    protected String date_time;         //日期和时间
    protected double amount;            //金额
    protected long rno;                 //流水编号

    public String getName() {
        return name;
    }

    public RunningAccountType getType() {
        return type;
    }

    public String getRemark() {
        return remark;
    }

    public boolean isDefaultRemark() {
        return isDefaultRemark;
    }

    public String getDate_time() {
        return date_time;
    }

    public double getAmount() {
        return amount;
    }

    public long getRno() {
        return rno;
    }

    public void setRno(long rno) {
        this.rno = rno;
    }
}

/**
 * 支出流水类
 */
class ExpenseRunningAccount extends RunningAccountBase {
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
        this.date_time = date_time;
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
        this.date_time = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
    }
}

/**
 * 收入流水类
 */
class IncomeRunningAccount extends RunningAccountBase {
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

/**
 * 转账流水类
 */
class TransferRunningAccount extends RunningAccountBase {
    protected final String default_remark = "一条转账记录";
    String exportAccount;   //转出账户
    String importAccount;   //转入账户

    /**
     * 不给定编号的构造方法
     *
     * @param remark        备注
     * @param date_time     日期
     * @param amount        金额
     * @param exportAccount 转出账户
     * @param importAccount 转入账户
     */
    public TransferRunningAccount(@NonNull String remark, String date_time, double amount, boolean isDefaultRemark, String exportAccount, String importAccount) {
        this.rno = -1;
        this.type = RunningAccountType.TRANSFER;
        this.name = "转账";
        this.remark = remark.isEmpty() ? default_remark : remark;
        this.date_time = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
        this.exportAccount = exportAccount;
        this.importAccount = importAccount;
    }

    /**
     * 给定编号的构造方法
     *
     * @param rno           编号
     * @param remark        备注
     * @param date_time     日期和时间
     * @param amount        金额
     * @param exportAccount 转出账户
     * @param importAccount 转入账户
     */
    public TransferRunningAccount(long rno, @NonNull String remark, String date_time, double amount, boolean isDefaultRemark, String exportAccount, String importAccount) {
        this.rno = rno;
        this.type = RunningAccountType.TRANSFER;
        this.name = "转账";
        this.remark = remark.isEmpty() ? default_remark : remark;
        this.date_time = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
        this.exportAccount = exportAccount;
        this.importAccount = importAccount;
    }
}
