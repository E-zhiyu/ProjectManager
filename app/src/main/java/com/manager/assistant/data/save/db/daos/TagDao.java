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
}
