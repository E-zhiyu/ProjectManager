package com.manager.assistant.data.classes.running_account;

import com.manager.assistant.auxiliary.enums.AccountType;

public abstract class RunningAccountBase {
    protected String title;             //名称
    protected AccountType type;  //种类
    protected String remark;            //备注
    protected String defaultRemark;     //默认备注
    protected String datetime;          //日期和时间
    protected double amount;            //金额
    protected long rno;                 //流水编号

    public RunningAccountBase() {
        defaultRemark = initDefaultRemark();
    }

    public String getTitle() {
        return title;
    }

    public AccountType getType() {
        return type;
    }

    public String getRemark() {
        return remark == null ? "" : remark;
    }

    public String getDefaultRemark() {
        return defaultRemark;
    }

    public String getDatetime() {
        return datetime;
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

    protected abstract String initDefaultRemark();
}

