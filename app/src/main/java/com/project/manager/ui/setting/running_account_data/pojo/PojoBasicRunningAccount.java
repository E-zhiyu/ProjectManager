package com.project.manager.ui.setting.running_account_data.pojo;

//流水基本数据POJO类
public class PojoBasicRunningAccount {
    private String type;        //种类
    private String remark;      //备注
    private String date_time;   //日期和时间
    private long tag_no;        //标签编号
    private double amount;      //金额
    private long rno;           //流水编号

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public long getTag_no() {
        return tag_no;
    }

    public void setTag_no(long tag_no) {
        this.tag_no = tag_no;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getRno() {
        return rno;
    }

    public void setRno(long rno) {
        this.rno = rno;
    }

    public PojoBasicRunningAccount(String type, String remark, String date_time, long tag_no, double amount, long rno) {
        this.type = type;
        this.remark = remark;
        this.date_time = date_time;
        this.tag_no = tag_no;
        this.amount = amount;
        this.rno = rno;
    }

    public PojoBasicRunningAccount() {

    }
}