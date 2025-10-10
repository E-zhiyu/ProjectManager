package com.project.manager.ui.bookkeeping;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BookKeepingViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public BookKeepingViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}