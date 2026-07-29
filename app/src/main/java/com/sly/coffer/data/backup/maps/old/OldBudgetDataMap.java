package com.sly.coffer.data.backup.maps.old;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sly.coffer.data.backup.pojo.old.OldBudgetPojo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class OldBudgetDataMap {
    private List<OldBudgetPojo> budget_data;

    public OldBudgetDataMap() {
    }

    public List<OldBudgetPojo> getBudget_data() {
        return budget_data;
    }

    public void setBudget_data(List<OldBudgetPojo> budget_data) {
        this.budget_data = budget_data;
    }
}
