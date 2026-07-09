package com.manager.assistant.ui.others.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.manager.assistant.data.save.db.entities.TagEntity;

import java.util.HashSet;
import java.util.Set;

public class TagSelectViewModel extends ViewModel {
    private final Set<TagEntity> checkedTagEntitySet = new HashSet<>();
    private final MutableLiveData<Boolean> needExecute = new MutableLiveData<>(false);  //是否需要进行更新

    public Set<TagEntity> getCheckedTagEntitySet() {
        return checkedTagEntitySet;
    }

    public MutableLiveData<Boolean> getNeedExecute() {
        return needExecute;
    }

    public void setNeedExecute(boolean needExecute) {
        this.needExecute.setValue(needExecute);
    }
}
