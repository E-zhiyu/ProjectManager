package com.manager.assistant.data.backup.helpers;

import android.content.Context;

import com.manager.assistant.auxiliary.enums.BackupDataType;
import com.manager.assistant.data.backup.maps.BudgetDataMap;
import com.manager.assistant.data.save.db.BookkeepingDb;

public class BudgetBackupHelper extends BackupHelperBase<BookkeepingDb, BudgetDataMap> {
    public BudgetBackupHelper(Context context) {
        super(context);
    }

    @Override
    protected Class<BudgetDataMap> getMapClass() {
        return BudgetDataMap.class;
    }

    @Override
    protected BookkeepingDb getDatabase(Context context) {
        return BookkeepingDb.getInstance(context);
    }

    @Override
    protected BudgetDataMap getAllDataInMap() {
        return db.dataBackupDao().exportBudgetData();
    }

    @Override
    protected void saveDataInMapToDb(BudgetDataMap map) {
        db.dataBackupDao().importBudgetData(map);
    }

    @Override
    protected String getTempDataFileName() {
        return BackupDataType.BUDGET.getFileName();
    }
}
