package com.manager.assistant.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "accountTagRef",
        primaryKeys = {"accountId", "tagId"},
        indices = {
                @Index(value = "accountId"),
                @Index(value = "tagId")
        }
)
public class AccountTagRefEntity {
    private long accountId;
    private long tagId;

    public AccountTagRefEntity(long accountId, long tagId) {
        this.accountId = accountId;
        this.tagId = tagId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }
}
