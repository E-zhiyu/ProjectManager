package com.manager.assistant.ui.data_communication.tag_modify;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.manager.assistant.data.data_class.Tag;

import java.util.ArrayList;
import java.util.List;

public class TagRepository {
    private static TagRepository instance;  //自身类的实例，确保通过getInstance()获取到的是同一个实例
    private TagUpdateReason updateReason;   //标签更新的原因
    private final MutableLiveData<List<Tag>> changedTagList = new MutableLiveData<>();  //修改的标签组成的列表(带有修改原因)

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
    public LiveData<List<Tag>> getChangedTagList() {
        return changedTagList;
    }

    /**
     * 更新单个标签数据
     *
     * @param tag_name     标签名称
     * @param tag_no       标签编号
     * @param updateReason 标签更新的原因
     */
    public void updateTag(String tag_name, long tag_no, TagUpdateReason updateReason) {
        this.updateReason = updateReason;

        List<Tag> tagList = new ArrayList<>();
        Tag modified_tag = new Tag(tag_name, tag_no, 0);
        tagList.add(modified_tag);
        changedTagList.setValue(tagList);
    }

    /**
     * 更新多个标签数据
     *
     * @param tagList      标签对象列表
     * @param updateReason 标签更新的原因
     */
    public void updateTag(List<Tag> tagList, TagUpdateReason updateReason) {
        this.updateReason = updateReason;
        changedTagList.setValue(tagList);
    }

    public TagUpdateReason getUpdateReason() {
        return updateReason;
    }
}
