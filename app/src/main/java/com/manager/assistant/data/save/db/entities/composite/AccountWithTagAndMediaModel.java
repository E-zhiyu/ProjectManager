package com.manager.assistant.data.save.db.entities.composite;

import androidx.room.Embedded;

import com.manager.assistant.data.save.db.entities.AccountEntity;
import com.manager.assistant.data.save.db.entities.MediaEntity;
import com.manager.assistant.data.save.db.entities.TagEntity;

import java.util.List;

public class AccountWithTagAndMediaModel {
    @Embedded
    private AccountEntity account;
    private List<TagEntity> tagList;
    private List<MediaEntity> mediaList;

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public List<TagEntity> getTagList() {
        return tagList;
    }

    public void setTagList(List<TagEntity> tagList) {
        this.tagList = tagList;
    }

    public List<MediaEntity> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<MediaEntity> mediaList) {
        this.mediaList = mediaList;
    }
}
