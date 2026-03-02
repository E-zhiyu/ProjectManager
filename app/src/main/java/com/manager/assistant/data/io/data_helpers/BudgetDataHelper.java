package com.manager.assistant.data.io.data_helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.manager.assistant.data.data_save.database.BookkeepingDbHelper;
import com.manager.assistant.data.data_save.database.Columns;
import com.manager.assistant.data.data_save.database.Tables;
import com.manager.assistant.data.io.maps.BudgetDataMap;
import com.manager.assistant.data.io.pojo.PojoBudget;

import java.util.ArrayList;
import java.util.List;

public class BudgetDataHelper extends DataHelperBase<BookkeepingDbHelper, BudgetDataMap> {
    public BudgetDataHelper(Context context) {
        super(context);
    }

    @Override
    protected Class<BudgetDataMap> getMapClass() {
        return BudgetDataMap.class;
    }

    @Override
    protected BookkeepingDbHelper createHelper(Context context) {
        return new BookkeepingDbHelper(context);
    }

    @Override
    protected BudgetDataMap getAllDataInMap() {
        BudgetDataMap map = new BudgetDataMap();

        List<PojoBudget> budgetList = getBudgetData();
        map.setBudget_data(budgetList);

        return map;
    }

    @Override
    protected void saveDataInMapToDb(@NonNull BudgetDataMap map) {
        List<PojoBudget> budgetList = map.getBudget_data();
        setBudgetData(budgetList);
    }

    @NonNull
    private List<PojoBudget> getBudgetData() {
        SQLiteDatabase db = dbHelper.openReadLink();
        Cursor budgetCursor = db.query(
                Tables.BUDGET.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        List<PojoBudget> budgetList = new ArrayList<>();
        while (budgetCursor.moveToNext()) {
            long bno = budgetCursor.getLong(budgetCursor.getColumnIndexOrThrow(Columns.BNO.toString()));
            String name = budgetCursor.getString(budgetCursor.getColumnIndexOrThrow(Columns.BUDGET_NAME.toString()));
            double initAmount = budgetCursor.getDouble(budgetCursor.getColumnIndexOrThrow(Columns.INIT_AMOUNT.toString()));
            double leftAmount = budgetCursor.getDouble(budgetCursor.getColumnIndexOrThrow(Columns.LEFT_AMOUNT.toString()));
            String startDate = budgetCursor.getString(budgetCursor.getColumnIndexOrThrow(Columns.START_DATE.toString()));
            String resetFrequency = budgetCursor.getString(budgetCursor.getColumnIndexOrThrow(Columns.RESET_FREQUENCY.toString()));
            List<Long> tagNoList = getBudgetTagData(bno, db);

            PojoBudget budget = new PojoBudget();
            budget.setBno(bno);
            budget.setName(name);
            budget.setInitAmount(initAmount);
            budget.setLeftAmount(leftAmount);
            budget.setStartDate(startDate);
            budget.setResetFrequency(resetFrequency);
            budget.setTagNoList(tagNoList);
            budgetList.add(budget);
        }

        budgetCursor.close();
        db.close();
        return budgetList;
    }

    private void setBudgetData(List<PojoBudget> budgetList) {
        SQLiteDatabase db = dbHelper.openWriteLink();

        //清空之前表的内容
        db.delete(Tables.BUDGET.toString(), null, null);

        if (budgetList == null) {
            return;
        }

        ContentValues values = new ContentValues();
        for (PojoBudget budget : budgetList) {
            //读取数据
            long bno = budget.getBno();
            String name = budget.getName();
            double initAmount = budget.getInitAmount();
            double leftAmount = budget.getLeftAmount();
            String startDate = budget.getStartDate();
            String resetFrequency = budget.getResetFrequency();
            List<Long> tagNoList = budget.getTagNoList();

            //写入数据
            values.put(Columns.BNO.toString(), bno);
            values.put(Columns.BUDGET_NAME.toString(), name);
            values.put(Columns.INIT_AMOUNT.toString(), initAmount);
            values.put(Columns.LEFT_AMOUNT.toString(), leftAmount);
            values.put(Columns.START_DATE.toString(), startDate);
            values.put(Columns.RESET_FREQUENCY.toString(), resetFrequency);
            db.insert(Tables.BUDGET.toString(), null, values);
            values.clear();
            setBudgetTagData(bno, tagNoList, db);
        }
    }

    @NonNull
    private List<Long> getBudgetTagData(long bno, @NonNull SQLiteDatabase db) {
        String[] columns = {
                Columns.TAG_NO.toString()
        };
        String selection = Columns.BNO + "=?";
        String[] selectionArgs = {String.valueOf(bno)};

        Cursor tagCursor = db.query(
                Tables.BUDGET_TAG.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        List<Long> tagNoList = new ArrayList<>();
        while (tagCursor.moveToNext()) {
            long tagNo = tagCursor.getLong(tagCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            tagNoList.add(tagNo);
        }

        tagCursor.close();
        return tagNoList;
    }

    private void setBudgetTagData(long bno, @NonNull List<Long> tagNoList, @NonNull SQLiteDatabase db) {
        //清空之前表的内容
        db.delete(Tables.BUDGET_TAG.toString(), null, null);

        ContentValues values = new ContentValues();
        for (long tagNo : tagNoList) {
            values.put(Columns.BNO.toString(), bno);
            values.put(Columns.TAG_NO.toString(), tagNo);
            db.insert(Tables.BUDGET_TAG.toString(), null, values);
            values.clear();
        }
    }

    /**
     * 删除所有预算数据
     *
     * @param context 上下文
     */
    public static void deleteAllData(Context context) {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        db.delete(Tables.BUDGET.toString(), null, null);
        db.delete(Tables.BUDGET_TAG.toString(), null, null);

        db.close();
    }
}
