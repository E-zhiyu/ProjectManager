package com.manager.assistant.ui.data_communication.account_picture;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.manager.assistant.data.data_class.Picture;

import java.util.ArrayList;
import java.util.List;

public class AccountPictureViewModel extends ViewModel {
    private final MutableLiveData<List<Picture>> pictureLiveData = new MutableLiveData<>(); //图片列表
    private final MutableLiveData<Boolean> adapterStatData = new MutableLiveData<>();       //适配器是否位于删除状态

    /**
     * 获取图片数据
     *
     * @return 包含图片数据的LiveData
     */
    public LiveData<List<Picture>> getPictureLiveData() {
        return pictureLiveData;
    }

    public LiveData<Boolean> getAdapterStatData() {
        return adapterStatData;
    }

    /**
     * 添加一个新图片
     *
     * @param newPicture 新图片实例
     */
    public void addPicture(Picture newPicture) {
        List<Picture> currentData = pictureLiveData.getValue();

        List<Picture> pictureList;
        if (currentData == null) {
            pictureList = new ArrayList<>();
        } else {
            pictureList = new ArrayList<>(currentData);
        }

        pictureList.add(newPicture);
        pictureLiveData.postValue(pictureList);
    }

    /**
     * 添加多个新图片
     *
     * @param newPictureList 新图片列表
     */
    public void addPicture(List<Picture> newPictureList) {
        List<Picture> currentData = pictureLiveData.getValue();

        List<Picture> pictureList;
        if (currentData == null) {
            pictureList = new ArrayList<>();
        } else {
            pictureList = new ArrayList<>(currentData);
        }

        pictureList.addAll(newPictureList);
        pictureLiveData.postValue(pictureList);
    }

    /**
     * 删除选中的图片
     *
     * @param pictureListAfterDelete 删除图片后的列表
     */
    public void deletePicture(List<Picture> pictureListAfterDelete) {
        List<Picture> pictureList = new ArrayList<>(pictureListAfterDelete);
        pictureLiveData.postValue(pictureList);
    }

    /**
     * 更新图片适配器状态
     *
     * @param isDeleteMode 更新后是否为删除图片模式
     */
    public void updateAdapterStat(boolean isDeleteMode) {
        adapterStatData.postValue(isDeleteMode);
    }
}
