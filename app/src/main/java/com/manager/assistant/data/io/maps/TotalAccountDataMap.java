package com.manager.assistant.data.io.maps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.manager.assistant.data.io.pojo.PojoBasicRunningAccount;
import com.manager.assistant.data.io.pojo.PojoPicture;
import com.manager.assistant.data.io.pojo.PojoTag;
import com.manager.assistant.data.io.pojo.PojoTagGroup;
import com.manager.assistant.data.io.pojo.PojoTransferRunningAccount;

import java.util.List;

//数据最外层Map结构（该POJO类被序列化后成为最外层的JSON字典）
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
public class TotalAccountDataMap {
    private List<PojoBasicRunningAccount> basic_data;           //基本流水数据
    private List<PojoTransferRunningAccount> transfer_data;     //转账特有的数据
    private List<PojoTag> tag_data;                             //标签数据
    private List<PojoTagGroup> tag_group_data;                  //标签分组数据
    private List<PojoPicture> picture_data;                     //图片数据

    public List<PojoBasicRunningAccount> getBasic_data() {
        return basic_data;
    }

    public void setBasic_data(List<PojoBasicRunningAccount> basic_data) {
        this.basic_data = basic_data;
    }

    public List<PojoTransferRunningAccount> getTransfer_data() {
        return transfer_data;
    }

    public void setTransfer_data(List<PojoTransferRunningAccount> transfer_data) {
        this.transfer_data = transfer_data;
    }

    public List<PojoTag> getTag_data() {
        return tag_data;
    }

    public void setTag_data(List<PojoTag> tag_data) {
        this.tag_data = tag_data;
    }

    public List<PojoTagGroup> getTag_group_data() {
        return tag_group_data;
    }

    public void setTag_group_data(List<PojoTagGroup> tag_group_data) {
        this.tag_group_data = tag_group_data;
    }

    public List<PojoPicture> getPicture_data() {
        return picture_data;
    }

    public void setPicture_data(List<PojoPicture> picture_data) {
        this.picture_data = picture_data;
    }

    public TotalAccountDataMap() {

    }
}
