package com.sly.coffer.data.save.db;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class DatabaseMigrations {
    //添加捕获通知相关的数据
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `capturedNotifications` (`notificationId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT, `content` TEXT, `packageName` TEXT, `appName` TEXT, `time` INTEGER)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_capturedNotifications_title` ON `capturedNotifications` (`title`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_capturedNotifications_content` ON `capturedNotifications` (`content`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_capturedNotifications_appName` ON `capturedNotifications` (`appName`)");
        }
    };

    //删除通知规则中无用的索引
    //通知规则添加是否启用的索引
    //添加无障碍记账规则
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("DROP INDEX IF EXISTS index_notificationRules_name");
            db.execSQL("DROP INDEX IF EXISTS index_notificationRules_packageName");
            db.execSQL("DROP INDEX IF EXISTS index_notificationRules_targetTitle");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notificationRules_enabled` ON `notificationRules` (`enabled`)");

            db.execSQL("CREATE TABLE IF NOT EXISTS `accessibilityRules` (`ruleId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `type` INTEGER NOT NULL, `enabled` INTEGER NOT NULL DEFAULT true, `packageName` TEXT, `targetActivity` TEXT, `viewId` TEXT)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_accessibilityRules_enabled` ON `accessibilityRules` (`enabled`)");
            db.execSQL("CREATE TABLE IF NOT EXISTS `accessibilityRuleTagRef` (`ruleId` INTEGER NOT NULL, `tagId` INTEGER NOT NULL, PRIMARY KEY(`ruleId`, `tagId`), FOREIGN KEY(`ruleId`) REFERENCES `accessibilityRules`(`ruleId`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`tagId`) REFERENCES `tags`(`tagId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            db.execSQL("CREATE TABLE IF NOT EXISTS `accessibilityRuleTransfers` (`ruleId` INTEGER NOT NULL, `exportAccount` TEXT, `importAccount` TEXT, PRIMARY KEY(`ruleId`), FOREIGN KEY(`ruleId`) REFERENCES `accessibilityRules`(`ruleId`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        }
    };
}
