package com.project.manager.ui.bookkeeping;

import com.project.manager.ui.bookkeeping.flow_modify.flow_fragments.FlowTypeEnum;

public abstract class FlowBase {
    String name;        //名称
    FlowTypeEnum type;  //种类
    String remark;      //备注
    String date_time;   //日期和时间
    String tag;         //标签
    double amount;      //金额
    long fno;           //流水标识
}

/**
 * 支出流水类
 */
class ExpenseFlow extends FlowBase {
    /**
     * 不给定编号的构造方法
     *
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     * @param tag       标签
     */
    public ExpenseFlow(String remark, String date_time, double amount, String tag) {
        this.type = FlowTypeEnum.EXPENSE;
        this.name = "支出";
        this.remark = remark;
        this.date_time = date_time;
        this.amount = amount;
        this.fno = -1;
        this.tag = tag;
    }

    /**
     * 给定编号的构造方法（用于数据库查询时）
     *
     * @param fno       编号
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     * @param tag       标签
     */
    public ExpenseFlow(long fno, String remark, String date_time, double amount, String tag) {
        this.fno = fno;
        this.type = FlowTypeEnum.EXPENSE;
        this.name = "支出";
        this.remark = remark;
        this.date_time = date_time;
        this.amount = amount;
        this.tag = tag;
    }
}

/**
 * 收入流水类
 */
class IncomeFlow extends FlowBase {
    /**
     * 不给定编号的构造方法
     *
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     * @param tag       标签
     */
    public IncomeFlow(String remark, String date_time, double amount, String tag) {
        this.fno = -1;
        this.type = FlowTypeEnum.INCOME;
        this.name = "收入";
        this.remark = remark;
        this.date_time = date_time;
        this.amount = amount;
        this.tag = tag;
    }

    /**
     * 给定编号的构造方法
     *
     * @param fno       编号
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     * @param tag       标签
     */
    public IncomeFlow(long fno, String remark, String date_time, double amount, String tag) {
        this.fno = fno;
        this.type = FlowTypeEnum.INCOME;
        this.name = "收入";
        this.remark = remark;
        this.date_time = date_time;
        this.amount = amount;
        this.tag = tag;
    }
}

/**
 * 转账流水类
 */
class TransferFlow extends FlowBase {
    String exportAccount;   //转出账户
    String importAccount;   //转入账户

    /**
     * 不给定编号的构造方法
     *
     * @param remark        备注
     * @param date_time     日期
     * @param amount        金额
     * @param tag           标签
     * @param exportAccount 转出账户
     * @param importAccount 转入账户
     */
    public TransferFlow(String remark, String date_time, double amount, String tag, String exportAccount, String importAccount) {
        this.fno = -1;
        this.type = FlowTypeEnum.TRANSFER;
        this.name = "转账";
        this.remark = remark;
        this.date_time = date_time;
        this.amount = amount;
        this.tag = tag;
        this.exportAccount = exportAccount;
        this.importAccount = importAccount;
    }

    /**
     * 给定编号的构造方法
     *
     * @param fno           编号
     * @param remark        备注
     * @param date_time     日期和时间
     * @param amount        金额
     * @param tag           标签
     * @param exportAccount 转出账户
     * @param importAccount 转入账户
     */
    public TransferFlow(long fno, String remark, String date_time, double amount, String tag, String exportAccount, String importAccount) {
        this.fno = fno;
        this.type = FlowTypeEnum.TRANSFER;
        this.name = "转账";
        this.remark = remark;
        this.date_time = date_time;
        this.amount = amount;
        this.tag = tag;
        this.exportAccount = exportAccount;
        this.importAccount = importAccount;
    }
}
