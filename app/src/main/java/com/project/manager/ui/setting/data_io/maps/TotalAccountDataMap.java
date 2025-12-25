package com.project.manager.ui.setting.data_io.maps;

import com.project.manager.ui.setting.data_io.pojo.PojoBasicRunningAccount;
import com.project.manager.ui.setting.data_io.pojo.PojoTag;
import com.project.manager.ui.setting.data_io.pojo.PojoTagGroup;
import com.project.manager.ui.setting.data_io.pojo.PojoTransferRunningAccount;

import java.util.List;

//数据最外层Map结构（该POJO类被序列化后成为最外层的JSON字典）
public class TotalAccountDataMap {
    private List<PojoBasicRunningAccount> basic_data;
    private List<PojoTransferRunningAccount> transfer_data;
    private List<PojoTag> tag_data;
    private List<PojoTagGroup> tag_group_data;

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

    public TotalAccountDataMap() {

    }
}
