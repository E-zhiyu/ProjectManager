package com.project.manager.ui.view_model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.project.manager.ui.bookkeeping.tag.Tag;

import java.util.ArrayList;
import java.util.List;

public class AccountTagViewModel extends AndroidViewModel {
    private final MutableLiveData<List<TagWithModifyID>> tag = new MutableLiveData<>();

    public AccountTagViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<List<TagWithModifyID>> getTag() {
        return tag;
    }

    /**
     * 更新单个标签数据
     *
     * @param tag_name 标签名称
     * @param tag_no   标签编号
     */
    public void updateTag(String tag_name, long tag_no, AccountTagModifyID modifyID) {
        List<TagWithModifyID> tagList = new ArrayList<>();
        TagWithModifyID modified_tag = new TagWithModifyID(tag_name, tag_no, modifyID);
        tagList.add(modified_tag);
        tag.setValue(tagList);
    }

    /**
     * 更新多个标签数据
     *
     * @param tagList 标签对象列表
     */
    public void updateTag(List<TagWithModifyID> tagList) {
        tag.setValue(tagList);
    }

    //重置标签数据防止重复观察
    public void resetTagValue() {
        tag.setValue(null);
    }
}
