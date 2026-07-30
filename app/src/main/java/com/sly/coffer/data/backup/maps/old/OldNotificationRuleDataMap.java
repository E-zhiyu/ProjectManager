package com.sly.coffer.data.backup.maps.old;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sly.coffer.data.backup.pojo.old.OldNotificationRulePojo;
import com.sly.coffer.data.backup.pojo.old.OldNotificationRuleTransferPojo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class OldNotificationRuleDataMap {
    private List<OldNotificationRuleTransferPojo> rule_account;
    private List<OldNotificationRulePojo> rule_data;

    public OldNotificationRuleDataMap() {
    }

    public List<OldNotificationRuleTransferPojo> getRule_account() {
        return rule_account;
    }

    public void setRule_account(List<OldNotificationRuleTransferPojo> rule_account) {
        this.rule_account = rule_account;
    }

    public List<OldNotificationRulePojo> getRule_data() {
        return rule_data;
    }

    public void setRule_data(List<OldNotificationRulePojo> rule_data) {
        this.rule_data = rule_data;
    }
}
