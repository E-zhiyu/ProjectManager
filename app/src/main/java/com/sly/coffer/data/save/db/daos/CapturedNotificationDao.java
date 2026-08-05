package com.sly.coffer.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.sly.coffer.data.save.db.entities.CapturedNotificationEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface CapturedNotificationDao {
    @Query("SELECT * FROM capturedNotifications " +
            "WHERE :useSearchFilter = 0 " +
            "OR title LIKE '%' || :keyword || '%' " +
            "OR content LIKE '%' || :keyword || '%' " +
            "OR appName LIKE '%' || :keyword || '%' " +
            "ORDER BY time DESC")
    Flowable<List<CapturedNotificationEntity>> getAllCapturedNotificationFlowable(String keyword, int useSearchFilter);

    /**
     * 清空捕获的通知
     *
     * @return 是否完成
     */
    @Query("DELETE FROM capturedNotifications")
    Completable clearCapturedNotification();

    /**
     * 添加捕获的通知
     * @param notification 被捕获的通知
     * @return 是否完成
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Completable insertCapturedNotification(CapturedNotificationEntity notification);

    /**
     * 删除捕获的通知
     *
     * @param notification 需要删除的通知
     * @return 是否完成
     */
    @Delete
    Completable deleteCapturedNotification(CapturedNotificationEntity notification);
}
