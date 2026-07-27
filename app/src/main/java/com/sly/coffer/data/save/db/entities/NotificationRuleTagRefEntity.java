package com.sly.coffer.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "notificationRuleTagRef",
        primaryKeys = {"ruleId", "tagId"},
        foreignKeys = {
                @ForeignKey(
                        entity = NotificationRuleEntity.class,
                        parentColumns = "ruleId",
                        childColumns = "ruleId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = TagEntity.class,
                        parentColumns = "tagId",
                        childColumns = "tagId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "ruleId"),
                @Index(value = "tagId")
        }
)
public class NotificationRuleTagRefEntity {
    private long ruleId;
    private long tagId;

    public NotificationRuleTagRefEntity(long ruleId, long tagId) {
        this.ruleId = ruleId;
        this.tagId = tagId;
    }

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }
}
