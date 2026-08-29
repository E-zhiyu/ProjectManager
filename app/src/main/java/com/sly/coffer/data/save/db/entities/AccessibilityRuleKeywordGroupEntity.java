package com.sly.coffer.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "accessibilityRuleKeywordGroups",
        foreignKeys = {
                @ForeignKey(
                        entity = AccessibilityRuleEntity.class,
                        parentColumns = "ruleId",
                        childColumns = "ruleId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index(value = "ruleId")
        }
)
public class AccessibilityRuleKeywordGroupEntity {
    @PrimaryKey(autoGenerate = true)
    private long keywordId; //主键
    private long ruleId;    //所属的无障碍规则的外键
    private String content; //组合内容

    public AccessibilityRuleKeywordGroupEntity(long ruleId, String content) {
        this.ruleId = ruleId;
        this.content = content;
    }

    public long getKeywordId() {
        return keywordId;
    }

    public void setKeywordId(long keywordId) {
        this.keywordId = keywordId;
    }

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
