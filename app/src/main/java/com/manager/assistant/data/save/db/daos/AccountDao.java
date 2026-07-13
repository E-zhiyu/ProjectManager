package com.manager.assistant.data.save.db.daos;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.manager.assistant.auxiliary.enums.AccountType;
import com.manager.assistant.data.save.db.entities.AccountTagRefEntity;
import com.manager.assistant.data.save.db.entities.AccountEntity;
import com.manager.assistant.data.save.db.entities.AccountTransferEntity;
import com.manager.assistant.data.save.db.entities.MediaEntity;
import com.manager.assistant.data.save.db.entities.composite.AccountWithDetailModel;
import com.manager.assistant.helpers.file.FileHelper;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface AccountDao {
    /**
     * 获取最早的记账日期
     *
     * @return 最早的记账日期
     */
    @Query("SELECT dateTime FROM accounts ORDER BY dateTime ASC LIMIT 1")
    Flowable<Optional<LocalDate>> getEarliestDateFlowable();

    /**
     * 获取在日期范围内的流水记录
     *
     * @param start 起始日期（包含）
     * @param end   结束日期（不包含）
     * @return 在日期范围内的流水记录
     */
    @Query("SELECT * FROM accounts WHERE dateTime >= :start AND dateTime < :end")
    Flowable<List<AccountEntity>> getAccountInDateRange(LocalDate start, LocalDate end);

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
    Single<Optional<AccountWithDetailModel>> getAccountWithDetailSingleById(long accountId);

    /**
     * 获取数据库中储存的转出和转入账户
     *
     * @return 包含所有转出和转入账户的列表
     */
    @Query("SELECT exportAccount FROM accountTransfers WHERE exportAccount IS NOT NULL " +
            "UNION " +
            "SELECT importAccount FROM accountTransfers WHERE importAccount IS NOT NULL " +
            "ORDER BY 1 ASC")
    Single<List<String>> getTransferAccountsSingle();

    /**
     * 插入新的流水记录
     *
     * @param account 流水记录实例
     * @return 新添加的流水记录分配的主键值
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long insertAccount(AccountEntity account);

    /**
     * 插入流水记录的转账账户数据
     *
     * @param entity 转账账户数据实体
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAccountTransfer(AccountTransferEntity entity);

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
     * @param transfer        转账账户数据（仅当记录类型为转账时会写入）
     * @param mediaEntityList 附带的位于永久目录下的媒体文件实体列表
     * @param tagIdList       与该记录绑定的标签的 ID 列表
     */
    @Transaction
    default void addAccount(AccountEntity account, AccountTransferEntity transfer, @NonNull List<MediaEntity> mediaEntityList, @NonNull List<Long> tagIdList) {
        Long accountId = insertAccount(account);

        //写入转账账户数据
        if (account.getType() == AccountType.TRANSFER.ordinal()) {
            transfer.setAccountId(accountId);
            insertAccountTransfer(transfer);
        }

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
     * 通过流水编号删除转账账户记录
     *
     * @param accountId 需要删除转账账户记录的流水编号
     */
    @Query("DELETE FROM accountTransfers WHERE accountId = :accountId")
    void deleteAccountTransferByAccountId(long accountId);

    /**
     * 通过流水编号删除与标签的映射关系
     *
     * @param accountId 待删除的映射关系的流水记录 ID
     */
    @Query("DELETE FROM accountTagRef WHERE accountId = :accountId")
    void deleteAccountTagRefByAccountId(long accountId);

    /**
     * 通过流水记录编号获取媒体文件 Uri
     *
     * @param accountId 流水记录 ID
     * @return 该流水记录的媒体文件的 Uri
     */
    @Query("SELECT fileUri FROM medias WHERE accountId = :accountId")
    List<Uri> getMediaUriByAccountId(long accountId);

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
     * @param transfer        转账账户数据（仅当记录类型为转账时会写入）
     * @param mediaEntityList 位于永久目录下的媒体文件实体列表，可能包含主键值为0的媒体实体
     * @param tagIdList       修改后的标签列表
     */
    @Transaction
    default void modifyAccount(
            @NonNull AccountEntity account,
            AccountTransferEntity transfer,
            @NonNull List<MediaEntity> mediaEntityList,
            @NonNull List<Long> tagIdList,
            Context context
    ) {
        long accountId = account.getAccountId();

        //获取在数据库中的媒体文件 Uri，并计算需要删除的媒体文件的 Uri
        Set<Uri> oldMediaUriSet = new HashSet<>(getMediaUriByAccountId(accountId));
        Set<Uri> newMediaUriSet = mediaEntityList.stream()
                .map(MediaEntity::getFileUri)
                .collect(Collectors.toSet());
        oldMediaUriSet.removeAll(newMediaUriSet);

        //更新流水记录
        updateAccount(account);

        //更新转账账户数据
        deleteAccountTransferByAccountId(accountId);
        if (account.getType() == AccountType.TRANSFER.ordinal()) {
            transfer.setAccountId(accountId);
            insertAccountTransfer(transfer);
        }

        //更新标签
        deleteAccountTagRefByAccountId(accountId);
        List<AccountTagRefEntity> tagRefEntityList = tagIdList.stream()
                .map(id -> new AccountTagRefEntity(accountId, id))
                .collect(Collectors.toList());
        insertAccountTagRef(tagRefEntityList);

        //更新媒体
        deleteMediaByAccountId(accountId);
        insertMedia(mediaEntityList);

        //删除旧媒体文件
        for (Uri uri : oldMediaUriSet) {
            FileHelper.deleteFile(uri, context);
        }
    }

    /**
     * 从数据库中删除流水记录
     *
     * @param account 需要删除的流水记录
     */
    @Delete
    void deleteAccount(AccountEntity account);

    /**
     * 删除流水记录的事务
     *
     * @param account 需要删除的流水记录
     * @param context 上下文
     */
    default void removeAccount(@NonNull AccountEntity account, Context context) {
        //获取媒体数据
        Set<Uri> uriSet = new HashSet<>(getMediaUriByAccountId(account.getAccountId()));

        //删除流水记录
        deleteAccount(account);

        //移除媒体文件
        for (Uri uri : uriSet) {
            FileHelper.deleteFile(uri, context);
        }
    }
}
