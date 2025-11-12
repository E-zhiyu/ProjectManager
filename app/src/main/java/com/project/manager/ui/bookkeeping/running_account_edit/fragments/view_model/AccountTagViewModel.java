package com.project.manager.ui.bookkeeping.running_account_edit.fragments.view_model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.project.manager.ui.bookkeeping.tag.Tag;

public class AccountTagViewModel extends AndroidViewModel {
    private final MutableLiveData<Tag> tag = new MutableLiveData<>();

    public AccountTagViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Tag> getTag() {
        return tag;
    }

    public void updateTag(String tag_name, long tag_no) {
        Tag modified_tag = new Tag(tag_name, tag_no);
        tag.setValue(modified_tag);
    }
}
