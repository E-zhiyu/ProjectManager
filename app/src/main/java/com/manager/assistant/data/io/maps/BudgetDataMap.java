package com.manager.assistant.data.io.maps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manager.assistant.data.io.pojo.PojoBudget;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class BudgetDataMap {
    List<PojoBudget> budget_data;

    public BudgetDataMap() {
    }

    public List<PojoBudget> getBudget_data() {
        return budget_data;
    }

    public void setBudget_data(List<PojoBudget> budget_data) {
        this.budget_data = budget_data;
    }
}
