package com.manager.assistant.ui.others.viewmodel;

import android.net.Uri;

import androidx.lifecycle.ViewModel;

public class MediaAddOptionViewModel extends ViewModel {
    private Uri cameraFileUri = null;   //相机拍照得到的临时图片 File 类型的 Uri
    private final UnPeekLiveData<Integer> clickEvent = new UnPeekLiveData<>();

    public UnPeekLiveData<Integer> getClickEvent() {
        return clickEvent;
    }

    public void setClickEvent(int eventCode) {
        this.clickEvent.setValue(eventCode);
    }

    public Uri getCameraFileUri() {
        return cameraFileUri;
    }

    public void setCameraFileUri(Uri cameraFileUri) {
        this.cameraFileUri = cameraFileUri;
    }
}
