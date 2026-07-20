package com.manager.assistant.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "budgetTagRef",
        primaryKeys = {"budgetId", "tagId"},
        indices = {
                @Index(value = "budgetId"),
                @Index(value = "tagId")
        }
)
public class BudgetTagRefEntity {
    private long budgetId;
    private long tagId;

    public BudgetTagRefEntity(long budgetId, long tagId) {
        this.budgetId = budgetId;
        this.tagId = tagId;
    }

    public long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(long budgetId) {
        this.budgetId = budgetId;
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }
}
