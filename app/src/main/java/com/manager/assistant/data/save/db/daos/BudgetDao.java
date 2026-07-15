package com.manager.assistant.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;

import com.manager.assistant.data.save.db.entities.BudgetEntity;

import java.time.LocalDate;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface BudgetDao {
    /**
     * 获取预算数量
     *
     * @return 预算数量，支持响应式更新
     */
    @Query("SELECT COUNT(*) FROM budgets")
    Flowable<Integer> getBudgetCountFlowable();

    /**
     * 获取所有预算，并按照起算日期倒序排序
     *
     * @return 由所有预算组成的列表，支持响应式更新
     */
    @Query("SELECT * FROM budgets ORDER BY startDate DESC")
    Flowable<List<BudgetEntity>> getAllBudgetFlowable();

    /**
     * 删除预算
     *
     * @param budget 需要删除的预算
     * @return 是否完成
     */
    @Delete
    Completable deleteBudget(BudgetEntity budget);

    /**
     * 通过预算 ID 重置预算
     *
     * @param budgetId    需要重置的预算的 ID
     * @param currentDate 当前日期
     * @return 是否完成
     */
    @Query("UPDATE budgets SET leftAmount = initAmount, startDate = :currentDate WHERE budgetId = :budgetId")
    Completable resetBudgetById(long budgetId, LocalDate currentDate);
}
