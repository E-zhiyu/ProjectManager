package com.manager.assistant.data.classes;

import com.manager.assistant.ui.pages.bookkeeping.budget.ResetFrequency;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Budget {
    private long bno;                       //预算编号
    private final String name;              //预算名称
    private final double initAmount;        //初始金额
    private double leftAmount;              //剩余金额
    private String startDate;               //起算日期
    private final ResetFrequency resetFrequency;    //重置频率
    private final List<Long> tagNoList;     //监听的标签的编号

    /**
     * 完整的构造方法
     *
     * @param bno            预算编号
     * @param name           预算名称
     * @param initAmount     初始金额
     * @param leftAmount     剩余金额
     * @param startDate      起算日期
     * @param resetFrequency 重置频率
     * @param tagNoList      标签编号列表
     */
    public Budget(
            long bno,
            String name,
            double initAmount,
            double leftAmount,
            String startDate,
            ResetFrequency resetFrequency,
            List<Long> tagNoList
    ) {
        this.bno = bno;
        this.name = name;
        this.initAmount = initAmount;
        this.leftAmount = leftAmount;
        this.startDate = startDate;
        this.resetFrequency = resetFrequency;
        this.tagNoList = tagNoList;
    }

    /**
     * 不指定编号的构造方法
     *
     * @param name           预算名称
     * @param initAmount     初始金额
     * @param leftAmount     剩余金额
     * @param startDate      起算日期
     * @param resetFrequency 重置频率
     * @param tagNoList      标签编号列表
     */
    public Budget(
            String name,
            double initAmount,
            double leftAmount,
            String startDate,
            ResetFrequency resetFrequency,
            List<Long> tagNoList
    ) {
        this(0, name, initAmount, leftAmount, startDate, resetFrequency, tagNoList);
    }

    /**
     * 不指定剩余金额的构造方法(剩余金额与初始金额相等
     *
     * @param name           预算名称
     * @param initAmount     初始金额
     * @param startDate      起算日期
     * @param resetFrequency 重置频率
     * @param tagNoList      标签编号列表
     */
    public Budget(
            String name,
            double initAmount,
            String startDate,
            ResetFrequency resetFrequency,
            List<Long> tagNoList
    ) {
        this(name, initAmount, initAmount, startDate, resetFrequency, tagNoList);
    }

    public long getBno() {
        return bno;
    }

    public void setBno(long bno) {
        this.bno = bno;
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

    public ResetFrequency getResetFrequency() {
        return resetFrequency;
    }

    public List<Long> getTagNoList() {
        return tagNoList;
    }

    /**
     * 重置预算实例的数据
     */
    public void reset() {
        leftAmount = initAmount;

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        startDate = now.format(formatter);
    }
}
