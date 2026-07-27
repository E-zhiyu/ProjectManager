package com.sly.coffer.data.backup.helpers;

import android.content.Context;

import com.sly.coffer.auxiliary.enums.BackupDataType;
import com.sly.coffer.data.backup.maps.BudgetDataMap;
import com.sly.coffer.data.save.db.BookkeepingDb;

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
