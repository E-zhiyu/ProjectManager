package com.manager.assistant.data.backup.maps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manager.assistant.data.backup.pojo.AccountPojo;
import com.manager.assistant.data.backup.pojo.AccountTagRefPojo;
import com.manager.assistant.data.backup.pojo.AccountTransferPojo;
import com.manager.assistant.data.backup.pojo.MediaPojo;
import com.manager.assistant.data.backup.pojo.TagGroupPojo;
import com.manager.assistant.data.backup.pojo.TagPojo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class RunningAccountDataMap {
    private List<AccountPojo> accountList;
    private List<AccountTagRefPojo> accountTagRefList;
    private List<AccountTransferPojo> accountTransferList;
    private List<MediaPojo> mediaList;
    private List<TagGroupPojo> tagGroupList;
    private List<TagPojo> tagList;

    public RunningAccountDataMap() {
    }

    public List<AccountPojo> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<AccountPojo> accountList) {
        this.accountList = accountList;
    }

    public List<AccountTagRefPojo> getAccountTagRefList() {
        return accountTagRefList;
    }

    public void setAccountTagRefList(List<AccountTagRefPojo> accountTagRefList) {
        this.accountTagRefList = accountTagRefList;
    }

    public List<AccountTransferPojo> getAccountTransferList() {
        return accountTransferList;
    }

    public void setAccountTransferList(List<AccountTransferPojo> accountTransferList) {
        this.accountTransferList = accountTransferList;
    }

    public List<MediaPojo> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<MediaPojo> mediaList) {
        this.mediaList = mediaList;
    }

    public List<TagGroupPojo> getTagGroupList() {
        return tagGroupList;
    }

    public void setTagGroupList(List<TagGroupPojo> tagGroupList) {
        this.tagGroupList = tagGroupList;
    }

    public List<TagPojo> getTagList() {
        return tagList;
    }

    public void setTagList(List<TagPojo> tagList) {
        this.tagList = tagList;
    }
}
