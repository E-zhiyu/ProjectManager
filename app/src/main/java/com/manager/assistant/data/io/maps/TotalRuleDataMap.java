package com.manager.assistant.data.io.maps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manager.assistant.data.io.pojo.PojoAnalysisRule;
import com.manager.assistant.data.io.pojo.PojoRuleAccount;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class TotalRuleDataMap {
    private List<PojoAnalysisRule> rule_data;
    private List<PojoRuleAccount> rule_account;

    public TotalRuleDataMap() {
    }

    public List<PojoAnalysisRule> getRule_data() {
        return rule_data;
    }

    public void setRule_data(List<PojoAnalysisRule> rule_data) {
        this.rule_data = rule_data;
    }

    public List<PojoRuleAccount> getRule_account() {
        return rule_account;
    }

    public void setRule_account(List<PojoRuleAccount> rule_account) {
        this.rule_account = rule_account;
    }
}
