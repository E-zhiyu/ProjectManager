package com.project.manager.ui.bookkeeping.report;

public class AccountSourceCard {
    private int percentage;             //此来源占支出/收入的比例
    private double amount;              //该来源的总金额
    private final String source_name;   //来源名称
    private final long source_no;       //来源编号（与标签编号相同）

    public AccountSourceCard(double amount, String source_name, long source_no) {
        this.amount = amount;
        this.source_name = source_name;
        this.source_no = source_no;
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

    public String getSource_name() {
        return source_name;
    }

    public long getSource_no() {
        return source_no;
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
