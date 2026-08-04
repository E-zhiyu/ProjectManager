package com.sly.coffer.data.save.db.daos;

import androidx.room.Dao;
import androidx.room.Query;

import io.reactivex.rxjava3.core.Completable;

@Dao
public interface CapturedNotificationDao {
    /**
     * 清空捕获的通知
     *
     * @return 是否完成
     */
    @Query("DELETE FROM capturedNotifications")
    Completable clearCapturedNotification();
}
