package com.project.manager.data.data_class;

public class MonthAccountInfo {
    private final double expense;   //月支出
    private final double income;    //月收入
    private int percentage = 0; //该月份支出/收入/结余占这一年的百分比

    /**
     * 每月收支数据
     *
     * @param expense 每月支出
     * @param income  每月收入
     */
    public MonthAccountInfo(double expense, double income) {
        this.expense = expense;
        this.income = income;
    }

    public double getExpense() {
        return expense;
    }

    public double getIncome() {
        return income;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
}
