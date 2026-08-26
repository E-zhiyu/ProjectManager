package com.sly.coffer.ui.others.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sly.coffer.data.save.db.entities.TagEntity;

public class TagSingleSelectViewModel extends ViewModel {
    MutableLiveData<TagEntity> clickedEntity = new MutableLiveData<>();

    public MutableLiveData<TagEntity> getClickedEntity() {
        return clickedEntity;
    }

    public void setClickedEntity(TagEntity clickedEntity) {
        this.clickedEntity.setValue(clickedEntity);
    }
}
