package com.project.manager.ui.setting.running_account_data.maps;

import com.project.manager.ui.setting.running_account_data.pojo.PojoAnalysisRule;

import java.util.List;

public class TotalRuleDataMap {
    private List<PojoAnalysisRule> rule_data;

    public TotalRuleDataMap() {
    }

    public List<PojoAnalysisRule> getRule_data() {
        return rule_data;
    }

    public void setRule_data(List<PojoAnalysisRule> rule_data) {
        this.rule_data = rule_data;
    }
}
