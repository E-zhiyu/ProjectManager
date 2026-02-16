package com.manager.assistant.ui.data_sync.account_picture;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.manager.assistant.data.data_class.Picture;

import java.util.ArrayList;
import java.util.List;

public class AccountPictureViewModel extends ViewModel {
    private final MutableLiveData<List<Boolean>> selectedPictureData = new MutableLiveData<>(); //删除图片时的图片选择状态
    private final MutableLiveData<Boolean> adapterStatData = new MutableLiveData<>();       //适配器是否位于删除状态
    private final MutableLiveData<List<Picture>> newPictureData = new MutableLiveData<>();  //添加图片时的新图片数据

    /**
     * 获取适配器状态数据（是否为图片删除模式）
     *
     * @return 适配器状态LiveData
     */
    public LiveData<Boolean> getAdapterStatData() {
        return adapterStatData;
    }

    /**
     * 获取图片选择状态数据
     *
     * @return 图片选择状态LiveData
     */
    public LiveData<List<Boolean>> getPictureSelectData() {
        return selectedPictureData;
    }

    /**
     * 获取新图片数据
     *
     * @return 包含新图片实例的LiveData
     */
    public LiveData<List<Picture>> getNewPictureData() {
        return newPictureData;
    }

    /**
     * 添加一个新图片
     *
     * @param newPicture 新图片实例
     */
    public void addPicture(Picture newPicture) {
        List<Picture> pictureList = new ArrayList<>();
        pictureList.add(newPicture);
        newPictureData.postValue(pictureList);
    }

    /**
     * 添加多个新图片
     *
     * @param newPictureList 新图片列表
     */
    public void addPicture(List<Picture> newPictureList) {
        List<Picture> pictureList = new ArrayList<>(newPictureList);
        newPictureData.postValue(pictureList);
    }

    /**
     * 删除选中的图片
     *
     * @param pictureSelectList 删除图片后的列表
     */
    public void deletePicture(List<Boolean> pictureSelectList) {
        List<Boolean> pictureList = new ArrayList<>(pictureSelectList);
        selectedPictureData.postValue(pictureList);
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
