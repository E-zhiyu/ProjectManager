package com.manager.assistant.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Query;

import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.entities.TagGroupEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface TagDao {
    /**
     * 获取所有标签
     *
     * @return 所有标签组成的列表，支持响应式更新
     */
    @Query("SELECT * FROM tags ORDER BY groupId")
    Flowable<List<TagEntity>> getAllTagFlowable();

    /**
     * 获取所有标签分组
     *
     * @return 由所有标签分组组成的列表，支持响应式更新
     */
    @Query("SELECT * FROM taggroups")
    Flowable<List<TagGroupEntity>> getAllTagGroupFlowable();
}
