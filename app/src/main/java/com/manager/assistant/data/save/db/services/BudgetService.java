package com.manager.assistant.data.save.db.services;

import com.manager.assistant.data.save.db.BookkeepingDb;
import com.manager.assistant.data.save.db.entities.BudgetEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;

public class BudgetService {
    /**
     * 添加预算
     *
     * @param budget    待添加的预算
     * @param tagIdList 与该预算绑定的标签的 ID 编号
     * @param db        数据库实例
     * @return 是否完成
     */
    public static Completable addBudget(BudgetEntity budget, List<Long> tagIdList, BookkeepingDb db) {
        return Completable.defer(() -> {
            db.budgetDao().addBudget(budget, tagIdList);
            return Completable.complete();
        });
    }

    /**
     * 修改预算
     *
     * @param budget    修改后的预算
     * @param tagIdList 与该预算绑定的标签的 ID 编号
     * @param db        数据库实例
     * @return 是否完成
     */
    public static Completable modifyBudget(BudgetEntity budget, List<Long> tagIdList, BookkeepingDb db) {
        return Completable.defer(() -> {
            db.budgetDao().modifyBudget(budget, tagIdList);
            return Completable.complete();
        });
    }
}
