package com.sly.coffer.data.save.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(
        tableName = "budgets",
        indices = {
                @Index(value = "budgetId"),
                @Index(value = "name")
        }
)
public class BudgetEntity {
    @PrimaryKey(autoGenerate = true)
    private long budgetId;          //主键
    private String name;            //名称
    private double initAmount;      //初始金额
    private double leftAmount;      //余额
    private LocalDate startDate;    //起算日期
    private int resetFrequency;     //重置频率
    @ColumnInfo(defaultValue = "10")
    private int lowBalanceRatio;    //余额抵预警百分比

    public BudgetEntity(String name, double initAmount, double leftAmount, LocalDate startDate, int resetFrequency, int lowBalanceRatio) {
        this.name = name;
        this.initAmount = initAmount;
        this.leftAmount = leftAmount;
        this.startDate = startDate;
        this.resetFrequency = resetFrequency;
        this.lowBalanceRatio = lowBalanceRatio;
    }

    public long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(long budgetId) {
        this.budgetId = budgetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getInitAmount() {
        return initAmount;
    }

    public void setInitAmount(double initAmount) {
        this.initAmount = initAmount;
    }

    public double getLeftAmount() {
        return leftAmount;
    }

    public void setLeftAmount(double leftAmount) {
        this.leftAmount = leftAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public int getResetFrequency() {
        return resetFrequency;
    }

    public void setResetFrequency(int resetFrequency) {
        this.resetFrequency = resetFrequency;
    }

    public int getLowBalanceRatio() {
        return lowBalanceRatio;
    }

    public void setLowBalanceRatio(int lowBalanceRatio) {
        this.lowBalanceRatio = lowBalanceRatio;
    }
}
