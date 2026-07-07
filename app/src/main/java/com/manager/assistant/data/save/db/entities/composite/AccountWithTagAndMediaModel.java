package com.manager.assistant.data.save.db.entities.composite;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.manager.assistant.data.save.db.entities.AccountEntity;
import com.manager.assistant.data.save.db.entities.AccountTagRefEntity;
import com.manager.assistant.data.save.db.entities.MediaEntity;
import com.manager.assistant.data.save.db.entities.TagEntity;

import java.util.List;

public class AccountWithTagAndMediaModel {
    @Embedded
    private final AccountEntity account;
    @Relation(
            parentColumn = "accountId",
            entityColumn = "tagId",
            associateBy = @Junction(AccountTagRefEntity.class)
    )
    private final List<TagEntity> tagList;
    @Relation(
            entity = MediaEntity.class,
            parentColumn = "accountId",
            entityColumn = "accountId"
    )
    private final List<MediaEntity> mediaList;

    public AccountWithTagAndMediaModel(AccountEntity account, List<TagEntity> tagList, List<MediaEntity> mediaList) {
        this.account = account;
        this.tagList = tagList;
        this.mediaList = mediaList;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public List<TagEntity> getTagList() {
        return tagList;
    }

    public List<MediaEntity> getMediaList() {
        return mediaList;
    }

}
