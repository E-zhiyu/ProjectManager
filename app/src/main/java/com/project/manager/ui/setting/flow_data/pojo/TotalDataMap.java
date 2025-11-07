package com.project.manager.ui.setting.flow_data.pojo;

import java.util.List;

//数据最外层Map结构（该POJO类被序列化后成为最外层的JSON字典）
public class TotalDataMap {
    private List<PojoBasicFlow> basic_data;
    private List<PojoTransferFlow> transfer_data;
    private List<PojoTag> tag_data;
    private List<PojoTagGroup> tag_group_data;

    public List<PojoBasicFlow> getBasic_data() {
        return basic_data;
    }

    public void setBasic_data(List<PojoBasicFlow> basic_data) {
        this.basic_data = basic_data;
    }

    public List<PojoTransferFlow> getTransfer_data() {
        return transfer_data;
    }

    public void setTransfer_data(List<PojoTransferFlow> transfer_data) {
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

    public TotalDataMap() {

    }
}
