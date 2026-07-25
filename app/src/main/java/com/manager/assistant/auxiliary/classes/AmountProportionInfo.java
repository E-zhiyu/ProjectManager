package com.manager.assistant.auxiliary.classes;

public class AmountProportionInfo {
    private final int percentage;       //此来源占支出/收入的比例
    private final double amount;        //该来源的总金额
    private final String name;          //来源名称

    public AmountProportionInfo(int percentage, double amount, String name) {
        this.percentage = percentage;
        this.amount = amount;
        this.name = name;
    }

    public int getPercentage() {
        return percentage;
    }

    public double getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }
}
