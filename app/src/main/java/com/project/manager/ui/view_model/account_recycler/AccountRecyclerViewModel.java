package com.project.manager.ui.view_model.account_recycler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AccountRecyclerViewModel extends ViewModel {
    private final MutableLiveData<Boolean> dataUpdateTrigger = new MutableLiveData<>(); //是否在记账子界面之外更新了流水记录

    /**
     * 获取数据更新的标识符
     *
     * @return 数据更新标识符
     */
    public LiveData<Boolean> getDataUpdateTrigger() {
        return dataUpdateTrigger;
    }

    /**
     * 将数据标记为已更新
     */
    public void triggerDataUpdate() {
        dataUpdateTrigger.postValue(true);
    }
}
