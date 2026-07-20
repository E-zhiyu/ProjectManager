package com.manager.assistant.data.backup.maps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manager.assistant.data.backup.pojo.BudgetPojo;
import com.manager.assistant.data.backup.pojo.BudgetTagRefPojo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class BudgetDataMap {
    private List<BudgetPojo> budgetList;
    private List<BudgetTagRefPojo> budgetTagRefList;

    public BudgetDataMap() {
    }

    public List<BudgetTagRefPojo> getBudgetTagRefList() {
        return budgetTagRefList;
    }

    public void setBudgetTagRefList(List<BudgetTagRefPojo> budgetTagRefList) {
        this.budgetTagRefList = budgetTagRefList;
    }

    public List<BudgetPojo> getBudgetList() {
        return budgetList;
    }

    public void setBudgetList(List<BudgetPojo> budgetList) {
        this.budgetList = budgetList;
    }
}
