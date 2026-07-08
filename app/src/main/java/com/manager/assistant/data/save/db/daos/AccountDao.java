package com.manager.assistant.data.save.db.daos;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.manager.assistant.data.save.db.entities.AccountTagRefEntity;
import com.manager.assistant.data.save.db.entities.AccountEntity;
import com.manager.assistant.data.save.db.entities.MediaEntity;
import com.manager.assistant.data.save.db.entities.composite.AccountWithDetailModel;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

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

    /**
     * 根据流水 ID 获取带有标签的流水记录
     *
     * @param accountId 需要获取的流水记录的 ID
     * @return 带有标签的流水记录
     */
    @Transaction
    @Query("SELECT * FROM accounts WHERE accountId = :accountId")
    Single<Optional<AccountWithDetailModel>> getAccountWithTagAndMediaSingleById(long accountId);

    /**
     * 插入新的流水记录
     *
     * @param account 流水记录实例
     * @return 新添加的流水记录分配的主键值
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long insertAccount(AccountEntity account);

    /**
     * 插入新的流水记录与标签映射关系
     *
     * @param refList 映射关系列表
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAccountTagRef(List<AccountTagRefEntity> refList);

    /**
     * 插入新媒体文件
     *
     * @param mediaList 新媒体文件列表
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMedia(List<MediaEntity> mediaList);

    /**
     * 流水记录添加事务
     *
     * @param account         新流水记录实体
     * @param mediaEntityList 附带的位于永久目录下的媒体文件实体列表
     * @param tagIdList       与该记录绑定的标签的 ID 列表
     */
    @Transaction
    default void addAccount(AccountEntity account, @NonNull List<MediaEntity> mediaEntityList, @NonNull List<Long> tagIdList) {
        Long accountId = insertAccount(account);

        //生成媒体实体列表并写入数据
        List<MediaEntity> availableMediaList = mediaEntityList.stream() //赋予流水记录编号
                .peek(mediaEntity -> mediaEntity.setAccountId(accountId))
                .collect(Collectors.toList());
        insertMedia(availableMediaList);

        //生成标签映射列表并写入数据
        List<AccountTagRefEntity> tagRefEntityList = tagIdList.stream()
                .map(id -> new AccountTagRefEntity(accountId, id))
                .collect(Collectors.toList());
        insertAccountTagRef(tagRefEntityList);
    }

    /**
     * 通过流水编号删除与标签的
     *
     * @param accountId 待删除的映射关系的流水记录 ID
     */
    @Query("DELETE FROM accountTagRef WHERE accountId = :accountId")
    void deleteAccountTagRefByAccountId(long accountId);

    /**
     * 通过流水记录编号删除媒体记录
     *
     * @param accountId 流水记录编号
     */
    @Query("DELETE FROM medias WHERE accountId = :accountId")
    void deleteMediaByAccountId(long accountId);

    /**
     * 更新流水记录
     *
     * @param account 新流水记录数据
     */
    @Update
    void updateAccount(AccountEntity account);

    /**
     * 修改流水记录的事务
     *
     * @param account         修改后的流水数据
     * @param mediaEntityList 位于永久目录下的媒体文件实体列表，可能包含主键值为0的媒体实体
     * @param tagIdList       修改后的标签列表
     */
    @Transaction
    default void modifyAccount(@NonNull AccountEntity account, @NonNull List<MediaEntity> mediaEntityList, @NonNull List<Long> tagIdList) {
        long accountId = account.getAccountId();

        //更新流水记录
        updateAccount(account);

        //更新标签
        deleteAccountTagRefByAccountId(accountId);
        List<AccountTagRefEntity> tagRefEntityList = tagIdList.stream()
                .map(id -> new AccountTagRefEntity(accountId, id))
                .collect(Collectors.toList());
        insertAccountTagRef(tagRefEntityList);

        //删除旧媒体
        deleteMediaByAccountId(accountId);
        insertMedia(mediaEntityList);
    }
}
