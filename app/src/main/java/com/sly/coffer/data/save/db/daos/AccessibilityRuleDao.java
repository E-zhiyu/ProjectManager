package com.sly.coffer.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Transaction;

import com.sly.coffer.data.save.db.entities.AccessibilityRuleEntity;
import com.sly.coffer.data.save.db.entities.composite.AccessibilityRuleWithDetailModel;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface AccessibilityRuleDao {
    @Transaction
    @Query("SELECT * FROM accessibilityRules WHERE enabled = 1")
    Flowable<List<AccessibilityRuleWithDetailModel>> getOpenedAccessibilityRuleWithDetailFlowable();

    /**
     * 删除无障碍规则
     *
     * @param rule 需要删除的无障碍规则
     * @return 是否完成
     */
    @Delete
    Completable deleteAccessibilityRule(AccessibilityRuleEntity rule);

    /**
     * 读取所有无障碍规则
     *
     * @return 无障碍规则列表，支持响应式更新
     */
    @Query("SELECT * FROM accessibilityRules ORDER BY type")
    Flowable<List<AccessibilityRuleEntity>> getAllAccessibilityRuleFlowable();

    /**
     * 设置通知规则是否启用
     *
     * @param enabled 是否启用
     * @param ruleId  需要更新的规则的 ID
     * @return 是否完成
     */
    @Query("UPDATE accessibilityRules SET enabled = :enabled WHERE ruleId = :ruleId")
    Completable setRuleEnabled(boolean enabled, long ruleId);
}
