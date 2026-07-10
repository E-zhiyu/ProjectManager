package com.manager.assistant.data.save.db.daos;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.entities.TagGroupEntity;
import com.manager.assistant.data.save.db.entities.composite.TagWithGroupModel;

import java.util.List;
import java.util.Optional;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface TagDao {
    /**
     * 获取标签总数
     *
     * @return 标签总数量，支持响应式更新
     */
    @Query("SELECT COUNT(*) FROM tags")
    Flowable<Integer> getTagCountFlowable();

    /**
     * 获取指定作用域内的所有标签
     *
     * @param scopePow 作用域标识符，传递0获取所有作用域的标签
     * @return 所有标签组成的列表，支持响应式更新
     */
    @Query("SELECT * FROM tags WHERE scope & :scopePow = 0 ORDER BY groupId")
    Flowable<List<TagEntity>> getAllTagFlowable(int scopePow);

    /**
     * 获取所有标签分组
     *
     * @return 由所有标签分组组成的列表，支持响应式更新
     */
    @Query("SELECT * FROM taggroups")
    Flowable<List<TagGroupEntity>> getAllTagGroupFlowable();

    /**
     * 插入标签分组
     *
     * @param entity 标签分组实例
     * @return 分配的主键值
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertTagGroup(TagGroupEntity entity);

    /**
     * 通过标签 ID 获取标签数据
     *
     * @param tagId 需要获取的标签的编号
     * @return 该编号对应的标签
     */
    @Query("SELECT * FROM tags WHERE tagId = :tagId")
    Single<Optional<TagWithGroupModel>> getTagWithGroupSingleById(long tagId);

    /**
     * 获取所有分组名称
     *
     * @return 由所有分组名称组成的列表
     */
    @Query("SELECT name FROM tagGroups")
    Single<List<String>> getAllTagGroupNameSingle();

    /**
     * 判断标签名称是否在数据库里
     *
     * @param tagName       需要检验的标签名称
     * @param exceptedTagId 需要排除的标签 ID，防止检测到自身存在
     * @return 是否存在
     */
    @Query("SELECT EXISTS(SELECT * FROM tags WHERE name = :tagName AND tagId != :exceptedTagId)")
    Single<Boolean> isTagNameInDb(String tagName, long exceptedTagId);

    /**
     * 通过分组名称获取分组编号
     *
     * @param groupName 分组名称
     * @return 与传入的名称匹配的分组编号
     */
    @Query("SELECT groupId FROM tagGroups WHERE name = :groupName")
    Optional<Long> getGroupIdByName(String groupName);

    /**
     * 通过分组名称获取分组编号，并在没有同名分组时自动创建分组
     *
     * @param groupName 分组名称
     * @return 与传入的名称匹配的分组编号
     */
    @Transaction
    default long getGroupIdByNameOrCreate(@NonNull String groupName) {
        if (groupName.isEmpty()) return -1;

        Optional<Long> groupIdOptional = getGroupIdByName(groupName);
        if (groupIdOptional.isEmpty()) {
            TagGroupEntity newGroup = new TagGroupEntity(groupName);
            return insertTagGroup(newGroup);
        } else {
            return groupIdOptional.get();
        }
    }

    /**
     * 插入标签
     *
     * @param tag 需要插入的标签数据
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTag(TagEntity tag);

    /**
     * 添加标签事务
     *
     * @param tag       新添加的标签实体
     * @param groupName 用户输入的分组名称
     */
    @Transaction
    default void addTag(@NonNull TagEntity tag, String groupName) {
        long groupId = getGroupIdByNameOrCreate(groupName);
        tag.setGroupId(groupId);
        insertTag(tag);
    }

    /**
     * 更新标签
     *
     * @param tag 修改后的标签数据
     */
    @Update
    void updateTag(TagEntity tag);

    /**
     * 修改标签事务
     *
     * @param tag       修改后的标签数据
     * @param groupName 分组名称
     */
    default void modifyTag(@NonNull TagEntity tag, String groupName) {
        long groupId = getGroupIdByNameOrCreate(groupName);
        tag.setGroupId(groupId);
        updateTag(tag);
    }
}
