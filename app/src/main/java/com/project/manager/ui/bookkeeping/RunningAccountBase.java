package com.project.manager.ui.bookkeeping;

import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountTypeEnum;

public abstract class RunningAccountBase {
    protected String name;        //名称
    protected RunningAccountTypeEnum type;  //种类
    protected String remark;      //备注
    protected String date_time;   //日期和时间
    protected long tag_no;        //标签编号
    protected double amount;      //金额
    protected long rno;           //流水编号

    public String getName() {
        return name;
    }

    public RunningAccountTypeEnum getType() {
        return type;
    }

    public String getRemark() {
        return remark;
    }

    public String getDate_time() {
        return date_time;
    }

    public long getTag_no() {
        return tag_no;
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
    /**
     * 不给定编号的构造方法
     *
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     * @param tag_no    标签
     */
    public ExpenseRunningAccount(String remark, String date_time, double amount, long tag_no) {
        this.type = RunningAccountTypeEnum.EXPENSE;
        this.name = "支出";
        this.remark = remark;
        this.date_time = date_time;
        this.amount = amount;
        this.rno = -1;
        this.tag_no = tag_no;
    }

    /**
     * 给定编号的构造方法（用于数据库查询时）
     *
     * @param rno       编号
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     * @param tag_no    标签
     */
    public ExpenseRunningAccount(long rno, String remark, String date_time, double amount, long tag_no) {
        this.rno = rno;
        this.type = RunningAccountTypeEnum.EXPENSE;
        this.name = "支出";
        this.remark = remark;
        this.date_time = date_time;
        this.amount = amount;
        this.tag_no = tag_no;
    }
}

/**
 * 收入流水类
 */
class IncomeRunningAccount extends RunningAccountBase {
    /**
     * 不给定编号的构造方法
     *
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     * @param tag_no    标签
     */
    public IncomeRunningAccount(String remark, String date_time, double amount, long tag_no) {
        this.rno = -1;
        this.type = RunningAccountTypeEnum.INCOME;
        this.name = "收入";
        this.remark = remark;
        this.date_time = date_time;
        this.amount = amount;
        this.tag_no = tag_no;
    }

    /**
     * 给定编号的构造方法
     *
     * @param rno       编号
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     * @param tag_no    标签
     */
    public IncomeRunningAccount(long rno, String remark, String date_time, double amount, long tag_no) {
        this.rno = rno;
        this.type = RunningAccountTypeEnum.INCOME;
        this.name = "收入";
        this.remark = remark;
        this.date_time = date_time;
        this.amount = amount;
        this.tag_no = tag_no;
    }
}

/**
 * 转账流水类
 */
class TransferRunningAccount extends RunningAccountBase {
    String exportAccount;   //转出账户
    String importAccount;   //转入账户

    /**
     * 不给定编号的构造方法
     *
     * @param remark        备注
     * @param date_time     日期
     * @param amount        金额
     * @param tag_no        标签
     * @param exportAccount 转出账户
     * @param importAccount 转入账户
     */
    public TransferRunningAccount(String remark, String date_time, double amount, long tag_no, String exportAccount, String importAccount) {
        this.rno = -1;
        this.type = RunningAccountTypeEnum.TRANSFER;
        this.name = "转账";
        this.remark = remark;
        this.date_time = date_time;
        this.amount = amount;
        this.tag_no = tag_no;
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
     * @param tag_no        标签
     * @param exportAccount 转出账户
     * @param importAccount 转入账户
     */
    public TransferRunningAccount(long rno, String remark, String date_time, double amount, long tag_no, String exportAccount, String importAccount) {
        this.rno = rno;
        this.type = RunningAccountTypeEnum.TRANSFER;
        this.name = "转账";
        this.remark = remark;
        this.date_time = date_time;
        this.amount = amount;
        this.tag_no = tag_no;
        this.exportAccount = exportAccount;
        this.importAccount = importAccount;
    }
}
