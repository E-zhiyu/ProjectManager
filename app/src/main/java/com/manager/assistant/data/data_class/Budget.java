package com.manager.assistant.data.data_class;

import com.manager.assistant.ui.pages.bookkeeping.budget.BudgetAddModifyActivity;

import java.util.List;

public class Budget {
    private final long bno;                 //预算编号
    private final String name;              //预算名称
    private final double initAmount;        //初始金额
    private final double leftAmount;        //剩余金额
    private final String startDate;         //起算日期
    private final BudgetAddModifyActivity.ResetFrequency resetFrequency;    //重置频率
    private final List<Long> tagNoList;     //监听的标签的编号

    public Budget(long bno, String name, double initAmount, double leftAmount, String startDate, BudgetAddModifyActivity.ResetFrequency resetFrequency, List<Long> tagNoList) {
        this.bno = bno;
        this.name = name;
        this.initAmount = initAmount;
        this.leftAmount = leftAmount;
        this.startDate = startDate;
        this.resetFrequency = resetFrequency;
        this.tagNoList = tagNoList;
    }

    public long getBno() {
        return bno;
    }

    public String getName() {
        return name;
    }

    public double getInitAmount() {
        return initAmount;
    }

    public double getLeftAmount() {
        return leftAmount;
    }

    public String getStartDate() {
        return startDate;
    }

    public BudgetAddModifyActivity.ResetFrequency getResetFrequency() {
        return resetFrequency;
    }

    public List<Long> getTagNoList() {
        return tagNoList;
    }
}
