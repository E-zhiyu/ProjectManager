package com.sly.coffer.data.save.db;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DatabaseMigrations {
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `capturedNotifications` (`notificationId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT, `content` TEXT, `packageName` TEXT, `appName` TEXT, `time` INTEGER NOT NULL)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_capturedNotifications_title` ON `capturedNotifications` (`title`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_capturedNotifications_content` ON `capturedNotifications` (`content`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_capturedNotifications_appName` ON `capturedNotifications` (`appName`)");
        }
    };
}
