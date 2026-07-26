package com.manager.assistant.data.backup;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.manager.assistant.data.backup.maps.BudgetDataMap;
import com.manager.assistant.data.backup.maps.NotificationRuleDataMap;
import com.manager.assistant.data.backup.maps.RunningAccountDataMap;
import com.manager.assistant.data.backup.pojo.AccountPojo;
import com.manager.assistant.data.backup.pojo.AccountTagRefPojo;
import com.manager.assistant.data.backup.pojo.AccountTransferPojo;
import com.manager.assistant.data.backup.pojo.BudgetPojo;
import com.manager.assistant.data.backup.pojo.BudgetTagRefPojo;
import com.manager.assistant.data.backup.pojo.MediaPojo;
import com.manager.assistant.data.backup.pojo.NotificationRulePojo;
import com.manager.assistant.data.backup.pojo.NotificationRuleTagRefPojo;
import com.manager.assistant.data.backup.pojo.NotificationRuleTransferPojo;
import com.manager.assistant.data.backup.pojo.TagGroupPojo;
import com.manager.assistant.data.backup.pojo.TagPojo;
import com.manager.assistant.data.save.db.entities.AccountEntity;
import com.manager.assistant.data.save.db.entities.AccountTagRefEntity;
import com.manager.assistant.data.save.db.entities.AccountTransferEntity;
import com.manager.assistant.data.save.db.entities.BudgetEntity;
import com.manager.assistant.data.save.db.entities.BudgetTagRefEntity;
import com.manager.assistant.data.save.db.entities.MediaEntity;
import com.manager.assistant.data.save.db.entities.NotificationRuleEntity;
import com.manager.assistant.data.save.db.entities.NotificationRuleTagRefEntity;
import com.manager.assistant.data.save.db.entities.NotificationRuleTransferEntity;
import com.manager.assistant.data.save.db.entities.TagEntity;
import com.manager.assistant.data.save.db.entities.TagGroupEntity;

import java.util.List;

@Dao
public interface DataBackupDao {
    /**
     * 导出流水记录数据
     *
     * @return 流水记录数据集合
     */
    @Transaction
    default RunningAccountDataMap exportRunningAccountData() {
        EntityPojoMapper mapper = EntityPojoMapper.INSTANCE;

        //流水记录数据
        List<AccountEntity> accountEntityList = readAccountData();
        List<AccountPojo> accountPojoList = mapper.toAccountPojoList(accountEntityList);

        //流水标签数据
        List<AccountTagRefEntity> accountTagRefEntityList = readAccountTagRefData();
        List<AccountTagRefPojo> accountTagRefPojoList = mapper.toAccountTagRefPojoList(accountTagRefEntityList);

        //流水转账账户数据
        List<AccountTransferEntity> accountTransferEntityList = readAccountTransferData();
        List<AccountTransferPojo> accountTransferPojoList = mapper.toAccountTransferPojoList(accountTransferEntityList);

        //媒体数据
        List<MediaEntity> mediaEntityList = readMediaData();
        List<MediaPojo> mediaPojoList = mapper.toMediaPojoList(mediaEntityList);

        //标签分组数据
        List<TagGroupEntity> tagGroupEntityList = readTagGroupData();
        List<TagGroupPojo> tagGroupPojoList = mapper.toTagGroupPojoList(tagGroupEntityList);

        //标签数据
        List<TagEntity> tagEntityList = readTagData();
        List<TagPojo> tagPojoList = mapper.toTagPojoList(tagEntityList);

        //实例化 Map 类
        RunningAccountDataMap map = new RunningAccountDataMap();
        map.setAccountList(accountPojoList);
        map.setAccountTagRefList(accountTagRefPojoList);
        map.setAccountTransferList(accountTransferPojoList);
        map.setMediaList(mediaPojoList);
        map.setTagGroupList(tagGroupPojoList);
        map.setTagList(tagPojoList);

        return map;
    }

    /**
     * 导入流水记录数据
     *
     * @param data 流水记录数据集合
     */
    @Transaction
    default void importRunningAccountData(RunningAccountDataMap data) {
        if (data == null) return;

        EntityPojoMapper mapper = EntityPojoMapper.INSTANCE;

        //清空旧数据
        clearAccountData();
        clearAccountTagRefData();
        clearAccountTransferData();
        clearMediaData();
        clearTagGroupData();
        clearTagData();

        //标签分组数据
        List<TagGroupPojo> tagGroupPojoList = data.getTagGroupList();
        if (tagGroupPojoList != null && !tagGroupPojoList.isEmpty()) {
            writeTagGroupData(mapper.toTagGroupEntityList(tagGroupPojoList));
        }

        //标签数据
        List<TagPojo> tagPojoList = data.getTagList();
        if (tagPojoList != null && !tagPojoList.isEmpty()) {
            writeTagData(mapper.toTagEntityList(tagPojoList));
        }

        //流水记录数据
        List<AccountPojo> accountPojoList = data.getAccountList();
        if (accountPojoList != null && !accountPojoList.isEmpty()) {
            writeAccountData(mapper.toAccountEntityList(accountPojoList));
        }

        //流水标签数据
        List<AccountTagRefPojo> accountTagRefPojoList = data.getAccountTagRefList();
        if (accountTagRefPojoList != null && !accountTagRefPojoList.isEmpty()) {
            writeAccountTagRefData(mapper.toAccountTagRefEntityList(accountTagRefPojoList));
        }

        //流水转账账户数据
        List<AccountTransferPojo> accountTransferPojoList = data.getAccountTransferList();
        if (accountTransferPojoList != null && !accountTransferPojoList.isEmpty()) {
            writeAccountTransferData(mapper.toAccountTransferEntityList(accountTransferPojoList));
        }

        //媒体数据
        List<MediaPojo> mediaPojoList = data.getMediaList();
        if (mediaPojoList != null && !mediaPojoList.isEmpty()) {
            writeMediaData(mapper.toMediaEntityList(mediaPojoList));
        }
    }

    /**
     * 导出通知规则数据
     *
     * @return 通知规则数据集合
     */
    @Transaction
    default NotificationRuleDataMap exportNotificationRuleData() {
        EntityPojoMapper mapper = EntityPojoMapper.INSTANCE;

        //通知规则数据
        List<NotificationRuleEntity> notificationRuleEntityList = readNotificationRuleData();
        List<NotificationRulePojo> notificationRulePojoList = mapper.toNotificationRulePojoList(notificationRuleEntityList);

        //绑定的标签数据
        List<NotificationRuleTagRefEntity> notificationRuleTagRefEntityList = readNotificationRuleTagRefData();
        List<NotificationRuleTagRefPojo> notificationRuleTagRefPojoList = mapper.toNotificationRuleTagRefPojoList(notificationRuleTagRefEntityList);

        //转账账户数据
        List<NotificationRuleTransferEntity> notificationRuleTransferEntityList = readNotificationRuleTransferData();
        List<NotificationRuleTransferPojo> notificationRuleTransferPojoList = mapper.toNotificationRuleTransferPojoList(notificationRuleTransferEntityList);

        //实例化 Map
        NotificationRuleDataMap map = new NotificationRuleDataMap();
        map.setNotificationRuleList(notificationRulePojoList);
        map.setNotificationRuleTagRefList(notificationRuleTagRefPojoList);
        map.setNotificationRuleTransferList(notificationRuleTransferPojoList);

        return map;
    }

    /**
     * 导入通知规则数据
     *
     * @param data 通知规则数据集合
     */
    @Transaction
    default void importNotificationRuleData(NotificationRuleDataMap data) {
        if (data == null) return;

        EntityPojoMapper mapper = EntityPojoMapper.INSTANCE;

        //清空旧数据
        clearNotificationRuleData();
        clearNotificationRuleTagRefData();
        clearNotificationRuleTransferData();

        //通知规则数据
        List<NotificationRulePojo> notificationRulePojoList = data.getNotificationRuleList();
        if (notificationRulePojoList != null && !notificationRulePojoList.isEmpty()) {
            writeNotificationRuleData(mapper.toNotificationRuleEntityList(notificationRulePojoList));
        }

        //绑定的标签数据
        List<NotificationRuleTagRefPojo> notificationRuleTagRefPojoList = data.getNotificationRuleTagRefList();
        if (notificationRuleTagRefPojoList != null && !notificationRuleTagRefPojoList.isEmpty()) {
            writeNotificationRuleTagRefData(mapper.toNotificationRuleTagRefEntityList(notificationRuleTagRefPojoList));
        }

        //转账账户数据
        List<NotificationRuleTransferPojo> notificationRuleTransferPojoList = data.getNotificationRuleTransferList();
        if (notificationRuleTransferPojoList != null && !notificationRuleTransferPojoList.isEmpty()) {
            writeNotificationRuleTransferData(mapper.toNotificationRuleTransferEntityList(notificationRuleTransferPojoList));
        }
    }

    /**
     * 导出预算数据
     *
     * @return 预算数据集合
     */
    @Transaction
    default BudgetDataMap exportBudgetData() {
        EntityPojoMapper mapper = EntityPojoMapper.INSTANCE;

        //预算数据
        List<BudgetEntity> budgetEntityList = readBudgetData();
        List<BudgetPojo> budgetPojoList = mapper.toBudgetPojoList(budgetEntityList);

        //预算标签数据
        List<BudgetTagRefEntity> budgetTagRefEntityList = readBudgetTagRefData();
        List<BudgetTagRefPojo> budgetTagRefPojoList = mapper.toBudgetTagRefPojoList(budgetTagRefEntityList);

        //实例化 Map
        BudgetDataMap map = new BudgetDataMap();
        map.setBudgetList(budgetPojoList);
        map.setBudgetTagRefList(budgetTagRefPojoList);

        return map;
    }

    /**
     * 导入预算数据
     *
     * @param data 预算数据集合
     */
    @Transaction
    default void importBudgetData(BudgetDataMap data) {
        if (data == null) return;

        EntityPojoMapper mapper = EntityPojoMapper.INSTANCE;

        //清空旧数据
        clearBudgetData();
        clearBudgetTagRefData();

        //预算数据
        List<BudgetPojo> budgetPojoList = data.getBudgetList();
        if (budgetPojoList != null && !budgetPojoList.isEmpty()) {
            writeBudgetData(mapper.toBudgetEntityList(budgetPojoList));
        }

        //预算标签数据
        List<BudgetTagRefPojo> budgetTagRefPojoList = data.getBudgetTagRefList();
        if (budgetTagRefPojoList != null && !budgetTagRefPojoList.isEmpty()) {
            writeBudgetTagRefData(mapper.toBudgetTagRefEntityList(budgetTagRefPojoList));
        }
    }

    /**
     * 读取流水记录数据
     *
     * @return 流水记录列表
     */
    @Query("SELECT * FROM accounts")
    List<AccountEntity> readAccountData();

    /**
     * 清空流水记录数据
     */
    @Query("DELETE FROM accounts")
    void clearAccountData();

    /**
     * 写入流水记录数据
     *
     * @param entityList 需要写入的流水记录数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeAccountData(List<AccountEntity> entityList);

    /**
     * 读取流水记录和标签映射关系数据
     *
     * @return 映射关系数据列表
     */
    @Query("SELECT * FROM accountTagRef")
    List<AccountTagRefEntity> readAccountTagRefData();

    /**
     * 清空流水标签引用数据
     */
    @Query("DELETE FROM accountTagRef")
    void clearAccountTagRefData();

    /**
     * 写入流水标签引用数据
     *
     * @param entityList 流水标签引用数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeAccountTagRefData(List<AccountTagRefEntity> entityList);

    /**
     * 读取流水记录的转账账户数据
     *
     * @return 转账账户数据列表
     */
    @Query("SELECT * FROM accounttransfers")
    List<AccountTransferEntity> readAccountTransferData();

    /**
     * 清空流水记录转账账户数据
     */
    @Query("DELETE FROM accountTransfers")
    void clearAccountTransferData();

    /**
     * 写入流水记录转账账户数据
     *
     * @param entityList 转账账户数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeAccountTransferData(List<AccountTransferEntity> entityList);

    /**
     * 读取媒体数据
     *
     * @return 媒体数据列表
     */
    @Query("SELECT * FROM medias")
    List<MediaEntity> readMediaData();

    /**
     * 清空媒体数据
     */
    @Query("DELETE FROM medias")
    void clearMediaData();

    /**
     * 写入媒体数据
     *
     * @param entityList 媒体数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeMediaData(List<MediaEntity> entityList);

    /**
     * 读取标签分组数据
     *
     * @return 标签分组列表
     */
    @Query("SELECT * FROM taggroups")
    List<TagGroupEntity> readTagGroupData();

    /**
     * 清空标签分组数据
     */
    @Query("DELETE FROM taggroups")
    void clearTagGroupData();

    /**
     * 写入标签分组数据
     *
     * @param entityList 标签分组数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeTagGroupData(List<TagGroupEntity> entityList);

    /**
     * 读取标签数据
     *
     * @return 标签列表
     */
    @Query("SELECT * FROM tags")
    List<TagEntity> readTagData();

    /**
     * 清空标签数据
     */
    @Query("DELETE FROM tags")
    void clearTagData();

    /**
     * 写入标签数据
     *
     * @param entityList 标签数据
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeTagData(List<TagEntity> entityList);

    /**
     * 读取通知规则数据
     *
     * @return 通知规则数据列表
     */
    @Query("SELECT * FROM notificationRules")
    List<NotificationRuleEntity> readNotificationRuleData();

    /**
     * 清空通知规则数据
     */
    @Query("DELETE FROM notificationRules")
    void clearNotificationRuleData();

    /**
     * 写入通知规则数据
     *
     * @param entityList 通知规则数据列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeNotificationRuleData(List<NotificationRuleEntity> entityList);

    /**
     * 读取通知规则绑定的标签数据
     *
     * @return 绑定的标签数据列表
     */
    @Query("SELECT * FROM notificationRuleTagRef")
    List<NotificationRuleTagRefEntity> readNotificationRuleTagRefData();

    /**
     * 清空通知规则绑定的标签数据
     */
    @Query("DELETE FROM notificationRuleTagRef")
    void clearNotificationRuleTagRefData();

    /**
     * 写入通知规则绑定的标签数据
     *
     * @param entityList 绑定的标签数据列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeNotificationRuleTagRefData(List<NotificationRuleTagRefEntity> entityList);

    /**
     * 读取通知规则转账账户数据
     *
     * @return 转账账户数据列表
     */
    @Query("SELECT * FROM notificationRuleTransfers")
    List<NotificationRuleTransferEntity> readNotificationRuleTransferData();

    /**
     * 清空通知规则转账账户数据
     */
    @Query("DELETE FROM notificationRuleTransfers")
    void clearNotificationRuleTransferData();

    /**
     * 写入通知规则转账账户数据
     *
     * @param entityList 转账账户数据列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeNotificationRuleTransferData(List<NotificationRuleTransferEntity> entityList);

    /**
     * 读取预算数据
     *
     * @return 预算列表
     */
    @Query("SELECT * FROM budgets")
    List<BudgetEntity> readBudgetData();

    /**
     * 清空预算数据
     */
    @Query("DELETE FROM budgets")
    void clearBudgetData();

    /**
     * 写入预算数据
     *
     * @param entityList 预算列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeBudgetData(List<BudgetEntity> entityList);

    /**
     * 读取预算标签数据
     *
     * @return 预算标签列表
     */
    @Query("SELECT * FROM budgetTagRef")
    List<BudgetTagRefEntity> readBudgetTagRefData();

    /**
     * 清空预算标签数据
     */
    @Query("DELETE FROM budgetTagRef")
    void clearBudgetTagRefData();

    /**
     * 写入预算标签数据
     *
     * @param entityList 预算标签数据列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void writeBudgetTagRefData(List<BudgetTagRefEntity> entityList);
}
