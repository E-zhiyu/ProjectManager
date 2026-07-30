package com.sly.coffer.ui.others.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sly.coffer.data.save.db.entities.TagEntity;

public class TagSingleSelectViewModel extends ViewModel {
    MutableLiveData<TagEntity> clickedTag = new MutableLiveData<>();

    public MutableLiveData<TagEntity> getClickedTag() {
        return clickedTag;
    }

    public void setClickedTag(TagEntity clickedTag) {
        this.clickedTag.setValue(clickedTag);
    }
}
