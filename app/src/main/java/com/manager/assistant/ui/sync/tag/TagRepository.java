package com.manager.assistant.ui.sync.tag;

import com.manager.assistant.data.classes.Tag;
import com.manager.assistant.ui.sync.UnPeekLiveData;

import java.util.ArrayList;
import java.util.List;

public class TagRepository {
    private static TagRepository instance;  //自身类的实例，确保通过getInstance()获取到的是同一个实例
    private TagUpdateReason updateReason;   //标签更新的原因
    private final UnPeekLiveData<List<Tag>> changedTagList = new UnPeekLiveData<>();  //修改的标签组成的列表(带有修改原因)

    /**
     * 获取 TagRepository 实例
     *
     * @return TagRepository 实例，在不同地方调用仍然能获取同一实例
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
    public UnPeekLiveData<List<Tag>> getChangedTagList() {
        return changedTagList;
    }

    public TagUpdateReason getUpdateReason() {
        return updateReason;
    }

    /**
     * 更新单个标签数据
     *
     * @param tagName      标签名称
     * @param tagNo        标签编号
     * @param updateReason 标签更新的原因
     */
    public void updateTag(String tagName, long tagNo, TagUpdateReason updateReason) {
        this.updateReason = updateReason;

        List<Tag> tagList = new ArrayList<>();
        Tag modifiedTag = new Tag(tagName, tagNo);
        tagList.add(modifiedTag);
        changedTagList.postValue(tagList);
    }

    /**
     * 更新标签但是不传递标签数据
     *
     * @param reason 更新标签的原因（CLEAR或REFRESH）
     */
    public void updateTag(TagUpdateReason reason) {
        updateTag("", 0, reason);
    }

    /**
     * 更新多个标签数据
     *
     * @param tagList      标签对象列表
     * @param updateReason 标签更新的原因
     */
    public void updateTag(List<Tag> tagList, TagUpdateReason updateReason) {
        this.updateReason = updateReason;
        changedTagList.postValue(tagList);
    }
}
