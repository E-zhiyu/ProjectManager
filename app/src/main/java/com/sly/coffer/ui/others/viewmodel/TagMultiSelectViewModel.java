package com.sly.coffer.ui.others.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashSet;
import java.util.Set;

public class TagMultiSelectViewModel extends ViewModel {
    private final Set<Long> checkedTagIdSet = new HashSet<>();
    private final MutableLiveData<Boolean> needExecute = new MutableLiveData<>(false);  //是否需要进行更新

    public Set<Long> getCheckedTagIdSet() {
        return checkedTagIdSet;
    }

    public MutableLiveData<Boolean> getNeedExecute() {
        return needExecute;
    }

    public void setNeedExecute(boolean needExecute) {
        this.needExecute.setValue(needExecute);
    }
}
