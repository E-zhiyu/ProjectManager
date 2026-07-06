package com.manager.assistant.data.save.db.entity;

import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "budgetTagRef",
        primaryKeys = {"budgetId", "tagId"},
        indices = {
                @Index(value = "budgetId")
        }
)
public class BudgetTagRefEntity {
    long budgetId;
    long tagId;
}
