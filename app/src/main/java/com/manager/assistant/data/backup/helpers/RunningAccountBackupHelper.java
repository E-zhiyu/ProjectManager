package com.manager.assistant.data.backup.helpers;

import android.content.Context;

import com.manager.assistant.auxiliary.enums.BackupDataType;
import com.manager.assistant.data.backup.maps.RunningAccountDataMap;
import com.manager.assistant.data.save.db.BookkeepingDb;

public class RunningAccountBackupHelper extends BackupHelperBase<BookkeepingDb, RunningAccountDataMap> {
    public RunningAccountBackupHelper(Context context) {
        super(context);
    }

    @Override
    protected Class<RunningAccountDataMap> getMapClass() {
        return RunningAccountDataMap.class;
    }

    @Override
    protected BookkeepingDb getDatabase(Context context) {
        return BookkeepingDb.getInstance(context);
    }

    @Override
    protected RunningAccountDataMap getAllDataInMap() {
        return db.dataBackupDao().exportRunningAccountData();
    }

    @Override
    protected void saveDataInMapToDb(RunningAccountDataMap map) {
        db.dataBackupDao().importRunningAccountData(map);
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.RUNNING_ACCOUNT.getFileName();
    }
}
