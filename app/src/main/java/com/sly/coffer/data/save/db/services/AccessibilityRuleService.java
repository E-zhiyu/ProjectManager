package com.sly.coffer.data.save.db.services;

import com.sly.coffer.data.save.db.BookkeepingDb;
import com.sly.coffer.data.save.db.entities.AccessibilityRuleEntity;
import com.sly.coffer.data.save.db.entities.AccessibilityRuleTransferEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;

public class AccessibilityRuleService {
    /**
     * 添加新无障碍规则
     * @param rule 新规则
     * @param transfer 新规则对应的转账账户数据
     * @param tagIdList 新规则的标签编号数据
     * @param db 数据库实例
     * @return 是否完成
     */
    public static Completable addNewAccessibilityRule(
            AccessibilityRuleEntity rule,
            AccessibilityRuleTransferEntity transfer,
            List<Long> tagIdList,
            BookkeepingDb db
    ) {
        return Completable.defer(() -> {
            db.accessibilityRuleDao().addAccessibilityRule(rule, transfer, tagIdList);
            return Completable.complete();
        });
    }

    /**
     * 修改无障碍规则
     * @param rule 修改后的规则
     * @param transfer 转账账户数据
     * @param tagIdList 标签编号数据
     * @param db 数据库实例
     * @return 是否完成
     */
    public static Completable modifyAccessibilityRule(
            AccessibilityRuleEntity rule,
            AccessibilityRuleTransferEntity transfer,
            List<Long> tagIdList,
            BookkeepingDb db
    ) {
        return Completable.defer(() -> {
            db.accessibilityRuleDao().modifyAccessibilityRule(rule, transfer, tagIdList);
            return Completable.complete();
        });
    }
}
