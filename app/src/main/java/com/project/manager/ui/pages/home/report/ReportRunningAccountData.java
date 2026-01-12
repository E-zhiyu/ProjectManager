package com.project.manager.ui.pages.home.report;

import com.project.manager.ui.pages.bookkeeping.running_account_edit.fragments.RunningAccountType;

//提供报表信息的流水数据类型
public class ReportRunningAccountData {
    private final RunningAccountType type;  //流水种类
    private final double amount;            //金额
    private final long tag_no;              //标签编号
    private final int month;                //月份

    public ReportRunningAccountData(RunningAccountType type, double amount, long tag_no, int month) {
        this.type = type;
        this.amount = amount;
        this.tag_no = tag_no;
        this.month = month;
    }

    public RunningAccountType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public long getTag_no() {
        return tag_no;
    }

    public int getMonth() {
        return month;
    }
}
