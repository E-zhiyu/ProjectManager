package com.manager.assistant.data.backup.helpers;

import android.content.Context;

import com.manager.assistant.auxiliary.enums.BackupDataType;
import com.manager.assistant.data.backup.maps.NotificationRuleDataMap;
import com.manager.assistant.data.save.db.BookkeepingDb;

public class NotificationRuleBackupHelper extends BackupHelperBase<BookkeepingDb, NotificationRuleDataMap> {
    public NotificationRuleBackupHelper(Context context) {
        super(context);
    }

    @Override
    protected Class<NotificationRuleDataMap> getMapClass() {
        return NotificationRuleDataMap.class;
    }

    @Override
    protected BookkeepingDb getDatabase(Context context) {
        return BookkeepingDb.getInstance(context);
    }

    @Override
    protected NotificationRuleDataMap getAllDataInMap() {
        return db.dataBackupDao().exportNotificationRuleData();
    }

    @Override
    protected void saveDataInMapToDb(NotificationRuleDataMap map) {
        db.dataBackupDao().importNotificationRuleData(map);
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.NOTIFICATION_RULE.getFileName();
    }
}
