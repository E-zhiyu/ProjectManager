package com.manager.assistant.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Query;

import com.manager.assistant.data.save.db.entities.NotificationRuleEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface RuleDao {
    /**
     * 获取通知规则数量
     *
     * @return 通知规则数量，支持响应式更新
     */
    @Query("SELECT COUNT(*) FROM notificationRules")
    Flowable<Integer> getNotificationRuleCountFlowable();

    /**
     * 获取所有通知规则
     *
     * @return 由通知规则组成的列表，支持响应式更新
     */
    @Query("SELECT * FROM notificationRules ORDER BY type")
    Flowable<List<NotificationRuleEntity>> getAllNotificationRuleFlowable();
}
