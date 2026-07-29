package com.sly.coffer.data.backup.maps.old;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sly.coffer.data.backup.pojo.old.OldAccountPojo;
import com.sly.coffer.data.backup.pojo.old.OldAccountTransferPojo;
import com.sly.coffer.data.backup.pojo.old.OldMediaPojo;
import com.sly.coffer.data.backup.pojo.old.OldTagGroupPojo;
import com.sly.coffer.data.backup.pojo.old.OldTagPojo;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class OldRunningAccountDataMap {
    private List<OldAccountPojo> basic_data;
    private List<OldMediaPojo> picture_data;
    private List<OldTagPojo> tag_data;
    private List<OldTagGroupPojo> tag_group_data;
    private List<OldAccountTransferPojo> transfer_data;

    public OldRunningAccountDataMap() {
    }

    public List<OldAccountPojo> getBasic_data() {
        return basic_data;
    }

    public void setBasic_data(List<OldAccountPojo> basic_data) {
        this.basic_data = basic_data;
    }

    public List<OldMediaPojo> getPicture_data() {
        return picture_data;
    }

    public void setPicture_data(List<OldMediaPojo> picture_data) {
        this.picture_data = picture_data;
    }

    public List<OldTagPojo> getTag_data() {
        return tag_data;
    }

    public void setTag_data(List<OldTagPojo> tag_data) {
        this.tag_data = tag_data;
    }

    public List<OldTagGroupPojo> getTag_group_data() {
        return tag_group_data;
    }

    public void setTag_group_data(List<OldTagGroupPojo> tag_group_data) {
        this.tag_group_data = tag_group_data;
    }

    public List<OldAccountTransferPojo> getTransfer_data() {
        return transfer_data;
    }

    public void setTransfer_data(List<OldAccountTransferPojo> transfer_data) {
        this.transfer_data = transfer_data;
    }
}
