package com.manager.assistant.data.save.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tagGroups",
        indices = {
                @Index(value = "groupId")
        }
)
public class TagGroupEntity {
    @PrimaryKey(autoGenerate = true)
    private long groupId;
    private String name;

    public TagGroupEntity(String name) {
        this.name = name;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
