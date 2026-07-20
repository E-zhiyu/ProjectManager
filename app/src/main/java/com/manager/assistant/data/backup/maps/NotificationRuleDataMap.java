package com.manager.assistant.data.backup.maps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manager.assistant.data.backup.pojo.NotificationRulePojo;
import com.manager.assistant.data.backup.pojo.NotificationRuleTagRefPojo;
import com.manager.assistant.data.backup.pojo.NotificationRuleTransferPojo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class NotificationRuleDataMap {
    private List<NotificationRulePojo> notificationRuleList;
    private List<NotificationRuleTagRefPojo> notificationRuleTagRefList;
    private List<NotificationRuleTransferPojo> notificationRuleTransferList;

    public NotificationRuleDataMap() {
    }

    public List<NotificationRulePojo> getNotificationRuleList() {
        return notificationRuleList;
    }

    public void setNotificationRuleList(List<NotificationRulePojo> notificationRuleList) {
        this.notificationRuleList = notificationRuleList;
    }

    public List<NotificationRuleTagRefPojo> getNotificationRuleTagRefList() {
        return notificationRuleTagRefList;
    }

    public void setNotificationRuleTagRefList(List<NotificationRuleTagRefPojo> notificationRuleTagRefList) {
        this.notificationRuleTagRefList = notificationRuleTagRefList;
    }

    public List<NotificationRuleTransferPojo> getNotificationRuleTransferList() {
        return notificationRuleTransferList;
    }

    public void setNotificationRuleTransferList(List<NotificationRuleTransferPojo> notificationRuleTransferList) {
        this.notificationRuleTransferList = notificationRuleTransferList;
    }
}
