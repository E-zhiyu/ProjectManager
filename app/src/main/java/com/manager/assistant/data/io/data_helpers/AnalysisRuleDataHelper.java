package com.manager.assistant.data.io.data_helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.manager.assistant.data.data_save.database.Columns;
import com.manager.assistant.data.data_save.database.BookkeepingDbHelper;
import com.manager.assistant.data.data_save.database.Tables;
import com.manager.assistant.data.io.pojo.PojoAnalysisRule;
import com.manager.assistant.data.io.maps.TotalRuleDataMap;
import com.manager.assistant.data.io.pojo.PojoRuleAccount;

import java.util.ArrayList;
import java.util.List;

public class AnalysisRuleDataHelper extends DataHelperBase<BookkeepingDbHelper, TotalRuleDataMap> {
    private final boolean isTagNoShouldWrite;

    /**
     * 读取数据时选用的构造方法
     *
     * @param context 上下文
     */
    public AnalysisRuleDataHelper(Context context) {
        super(context);
        this.isTagNoShouldWrite = false;
    }

    /**
     * 写入数据时选用的构造方法
     *
     * @param context            上下文
     * @param isTagNoShouldWrite 是否需要写入tag_no属性
     */
    public AnalysisRuleDataHelper(Context context, boolean isTagNoShouldWrite) {
        super(context);
        this.isTagNoShouldWrite = isTagNoShouldWrite;
    }

    @Override
    protected BookkeepingDbHelper createHelper() {
        return new BookkeepingDbHelper(context);
    }

    @Override
    protected Class<TotalRuleDataMap> getMapClass() {
        return TotalRuleDataMap.class;
    }

    @Override
    protected void saveDataInMapToDb(@NonNull TotalRuleDataMap map) {
        List<PojoAnalysisRule> ruleList = map.getRule_data();
        List<PojoRuleAccount> ruleAccountList = map.getRule_account();

        //将对应的数据写入数据库
        setRuleData(ruleList);
        setRuleAccount(ruleAccountList);
    }

    @Override
    protected TotalRuleDataMap getAllDataInMap() {
        List<PojoAnalysisRule> ruleList = getRuleData();
        List<PojoRuleAccount> ruleAccountList = getRuleAccount();

        TotalRuleDataMap totalRuleDataMap = new TotalRuleDataMap();
        totalRuleDataMap.setRule_data(ruleList);
        totalRuleDataMap.setRule_account(ruleAccountList);

        return totalRuleDataMap;
    }

    @NonNull
    private List<PojoAnalysisRule> getRuleData() {
        List<PojoAnalysisRule> ruleList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.openReadLink();

        Cursor ruleCursor = db.query(
                Tables.ANALYSIS_RULE.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (ruleCursor.moveToNext()) {
            String ruleName = ruleCursor.getString(ruleCursor.getColumnIndexOrThrow(Columns.RULE_NAME.toString()));
            long rule_no = ruleCursor.getLong(ruleCursor.getColumnIndexOrThrow(Columns.RULE_NO.toString()));
            long tag_no = ruleCursor.getLong(ruleCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            String type = ruleCursor.getString(ruleCursor.getColumnIndexOrThrow(Columns.TYPE.toString()));
            String packageName = ruleCursor.getString(ruleCursor.getColumnIndexOrThrow(Columns.PACKAGE_NAME.toString()));
            String title = ruleCursor.getString(ruleCursor.getColumnIndexOrThrow(Columns.NOTIFICATION_TITLE.toString()));
            String content = ruleCursor.getString(ruleCursor.getColumnIndexOrThrow(Columns.NOTIFICATION_CONTENT.toString()));

            PojoAnalysisRule rule = new PojoAnalysisRule(ruleName, rule_no, tag_no, type, packageName, title, content);
            ruleList.add(rule);
        }

        ruleCursor.close();
        db.close();
        return ruleList;
    }

    private void setRuleData(List<PojoAnalysisRule> ruleList) {
        SQLiteDatabase db = dbHelper.openWriteLink();

        //清空之前表的内容
        db.delete(Tables.ANALYSIS_RULE.toString(), null, null);

        if (ruleList == null) {
            return;
        }

        for (PojoAnalysisRule rule : ruleList) {
            String ruleName = rule.getRuleName();
            long rule_no = rule.getRuleNo();
            long tag_no = rule.getTag_no();
            String type = rule.getType();
            String packageName = rule.getPackageName();
            String title = rule.getTitle();
            String content = rule.getContent();

            ContentValues ruleValues = new ContentValues();
            ruleValues.put(Columns.RULE_NAME.toString(), ruleName);
            ruleValues.put(Columns.RULE_NO.toString(), rule_no);
            if (isTagNoShouldWrite) {
                ruleValues.put(Columns.TAG_NO.toString(), tag_no);
            }
            ruleValues.put(Columns.TYPE.toString(), type);
            ruleValues.put(Columns.PACKAGE_NAME.toString(), packageName);
            ruleValues.put(Columns.NOTIFICATION_TITLE.toString(), title);
            ruleValues.put(Columns.NOTIFICATION_CONTENT.toString(), content);
            db.insert(Tables.ANALYSIS_RULE.toString(), null, ruleValues);
        }

        db.close();
    }

    @NonNull
    private List<PojoRuleAccount> getRuleAccount() {
        List<PojoRuleAccount> ruleAccountList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.openReadLink();

        Cursor ruleAccountCursor = db.query(
                Tables.RULE_ACCOUNT.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (ruleAccountCursor.moveToNext()) {
            long rule_no = ruleAccountCursor.getLong(ruleAccountCursor.getColumnIndexOrThrow(Columns.RULE_NO.toString()));
            String exportAccount = ruleAccountCursor.getString(ruleAccountCursor.getColumnIndexOrThrow(Columns.EXPORT.toString()));
            String importAccount = ruleAccountCursor.getString(ruleAccountCursor.getColumnIndexOrThrow(Columns.IMPORT.toString()));
            PojoRuleAccount ruleAccount = new PojoRuleAccount();
            ruleAccount.setRuleNo(rule_no);
            ruleAccount.setExportAccount(exportAccount);
            ruleAccount.setImportAccount(importAccount);
            ruleAccountList.add(ruleAccount);
        }

        ruleAccountCursor.close();
        db.close();
        return ruleAccountList;
    }

    private void setRuleAccount(List<PojoRuleAccount> ruleAccountList) {
        SQLiteDatabase db = dbHelper.openWriteLink();
        db.delete(Tables.RULE_ACCOUNT.toString(), null, null);

        if (ruleAccountList == null) {
            return;
        }

        for (PojoRuleAccount ruleAccount : ruleAccountList) {
            long rule_no = ruleAccount.getRuleNo();
            String exportAccount = ruleAccount.getExportAccount();
            String importAccount = ruleAccount.getImportAccount();

            ContentValues values = new ContentValues();
            values.put(Columns.RULE_NO.toString(), rule_no);
            values.put(Columns.EXPORT.toString(), exportAccount);
            values.put(Columns.IMPORT.toString(), importAccount);
            db.insert(Tables.RULE_ACCOUNT.toString(), null, values);
        }
    }

    public static void resetRule(Context context) {
        String tipStr = "数据清除失败：未知原因";
        try {
            BookkeepingDbHelper db_helper = new BookkeepingDbHelper(context);
            SQLiteDatabase db = db_helper.openWriteLink();

            db.delete(Tables.ANALYSIS_RULE.toString(), null, null);

            //恢复默认规则
            BookkeepingDbHelper.addDefaultRule(db);

            db.close();
            tipStr = "规则重置成功";
        } catch (SQLiteException e) {
            tipStr = "规则重置失败：数据库异常";
        } finally {
            Toast.makeText(context, tipStr, Toast.LENGTH_SHORT).show();
        }
    }
}
