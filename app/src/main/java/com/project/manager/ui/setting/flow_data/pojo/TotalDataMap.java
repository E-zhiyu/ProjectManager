package com.project.manager.ui.setting.flow_data.pojo;

import java.util.List;

//数据最外层Map结构（该POJO类被序列化后成为最外层的JSON字典）
public class TotalDataMap {
    private List<BasicFlowData> basic_data;
    private List<TransferFlowData> transfer_data;
    private List<TagData> tag_data;
    private List<TagGroupData> tag_group_data;

    public List<BasicFlowData> getBasic_data() {
        return basic_data;
    }

    public void setBasic_data(List<BasicFlowData> basic_data) {
        this.basic_data = basic_data;
    }

    public List<TransferFlowData> getTransfer_data() {
        return transfer_data;
    }

    public void setTransfer_data(List<TransferFlowData> transfer_data) {
        this.transfer_data = transfer_data;
    }

    public List<TagData> getTag_data() {
        return tag_data;
    }

    public void setTag_data(List<TagData> tag_data) {
        this.tag_data = tag_data;
    }

    public List<TagGroupData> getTag_group_data() {
        return tag_group_data;
    }

    public void setTag_group_data(List<TagGroupData> tag_group_data) {
        this.tag_group_data = tag_group_data;
    }

    public TotalDataMap() {

    }
}
