package com.manager.assistant.data.save.db.entities.composite;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.manager.assistant.data.save.db.entities.NotificationRuleEntity;
import com.manager.assistant.data.save.db.entities.NotificationRuleTagRefEntity;
import com.manager.assistant.data.save.db.entities.NotificationRuleTransferEntity;
import com.manager.assistant.data.save.db.entities.TagEntity;

import java.util.List;

public class NotificationRuleWithDetailModel {
    @Embedded
    private final NotificationRuleEntity rule;
    @Relation(
            entity = NotificationRuleTransferEntity.class,
            parentColumn = "ruleId",
            entityColumn = "ruleId"
    )
    private final NotificationRuleTransferEntity transfer;
    @Relation(
            parentColumn = "ruleId",
            entityColumn = "tagId",
            associateBy = @Junction(NotificationRuleTagRefEntity.class)
    )
    private final List<TagEntity> tagList;

    public NotificationRuleWithDetailModel(NotificationRuleEntity rule, NotificationRuleTransferEntity transfer, List<TagEntity> tagList) {
        this.rule = rule;
        this.transfer = transfer;
        this.tagList = tagList;
    }

    public NotificationRuleEntity getRule() {
        return rule;
    }

    public NotificationRuleTransferEntity getTransfer() {
        return transfer;
    }

    public List<TagEntity> getTagList() {
        return tagList;
    }
}
