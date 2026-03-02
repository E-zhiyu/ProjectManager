package com.manager.assistant.data.io.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class PojoBudget {
    private long bno;
    private String name;
    private double initAmount;
    private double leftAmount;
    private String startDate;
    private String resetFrequency;
    private List<Long> tagNoList;

    public PojoBudget() {
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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String  getResetFrequency() {
        return resetFrequency;
    }

    public void setResetFrequency(String  resetFrequency) {
        this.resetFrequency = resetFrequency;
    }

    public List<Long> getTagNoList() {
        return tagNoList;
    }

    public void setTagNoList(List<Long> tagNoList) {
        this.tagNoList = tagNoList;
    }
}
