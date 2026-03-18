package com.manager.assistant.data.classes;

public class AccountSourceInfo {
    private int percentage;             //此来源占支出/收入的比例
    private double amount;              //该来源的总金额
    private final String name;          //来源名称
    private final long sourceNo;        //来源编号（与标签编号相同）

    public AccountSourceInfo(double amount, String name, long sourceNo) {
        this.amount = amount;
        this.name = name;
        this.sourceNo = sourceNo;
    }

    public int getPercentage() {
        return percentage;
    }

    public double getAmount() {
        return amount;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public String getName() {
        return name;
    }

    public long getSourceNo() {
        return sourceNo;
    }

    /**
     * 增加金额
     *
     * @param amount 金额增加的量
     */
    public void amountAdd(double amount) {
        this.amount += amount;
    }
}
