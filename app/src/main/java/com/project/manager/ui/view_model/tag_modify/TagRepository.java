package com.project.manager.ui.view_model.tag_modify;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class TagRepository {
    private static TagRepository instance;  //自身类的实例，确保通过getInstance()获取到的是同一个实例
    private final MutableLiveData<List<TagWithModifyID>> changedTagList = new MutableLiveData<>();  //修改的标签组成的列表(带有修改原因)

    /**
     * 获取TagRepository实例
     *
     * @return TagRepository实例，在不同地方调用仍然能获取同一实例
     */
    public static TagRepository getInstance() {
        if (instance == null) {
            instance = new TagRepository();
        }
        return instance;
    }

    /**
     * 获取带有修改原因的标签列表
     *
     * @return 带有修改原因的标签列表
     */
    public LiveData<List<TagWithModifyID>> getChangedTagList() {
        return changedTagList;
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
        changedTagList.setValue(tagList);
    }

    /**
     * 更新多个标签数据
     *
     * @param tagList 标签对象列表
     */
    public void updateTag(List<TagWithModifyID> tagList) {
        changedTagList.setValue(tagList);
    }
}
