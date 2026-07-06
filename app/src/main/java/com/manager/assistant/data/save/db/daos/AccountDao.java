package com.manager.assistant.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.manager.assistant.data.save.db.entities.AccountTagRefEntity;
import com.manager.assistant.data.save.db.entities.AccountEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface AccountDao {
    /**
     * 获取符合过滤条件的流水记录
     *
     * @param query 数据库查询实例
     * @return 符合过滤条件的流水记录列表，支持响应式更新
     */
    @RawQuery(observedEntities = {AccountEntity.class, AccountTagRefEntity.class})
    Flowable<List<AccountEntity>> getAccountWithFilter(SupportSQLiteQuery query);
}
