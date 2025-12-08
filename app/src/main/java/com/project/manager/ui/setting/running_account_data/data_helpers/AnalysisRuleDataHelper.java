package com.project.manager.ui.setting.running_account_data.data_helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.project.manager.data.data_save.database.BookKeepingColumns;
import com.project.manager.data.data_save.database.BookKeepingDatabaseHelper;
import com.project.manager.data.data_save.database.BookKeepingTables;
import com.project.manager.ui.setting.running_account_data.pojo.PojoAnalysisRule;
import com.project.manager.ui.setting.running_account_data.maps.TotalRuleDataMap;

import java.util.ArrayList;
import java.util.List;

public class AnalysisRuleDataHelper extends DataHelperBase<BookKeepingDatabaseHelper, TotalRuleDataMap> {
    public AnalysisRuleDataHelper(Context context) {
        super(context);
    }

    @Override
    protected BookKeepingDatabaseHelper createHelper() {
        return new BookKeepingDatabaseHelper(context);
    }

    @Override
    protected Class<TotalRuleDataMap> getMapClass() {
        return TotalRuleDataMap.class;
    }

    @Override
    protected void saveDataInMapToDb(@NonNull TotalRuleDataMap map) {
        List<PojoAnalysisRule> ruleList = map.getRule_data();

        //将对应的数据写入数据库
        setRuleData(ruleList);
    }

    @Override
    protected TotalRuleDataMap getAllDataInMap() {
        List<PojoAnalysisRule> ruleList = getRuleData();

        TotalRuleDataMap totalRuleDataMap = new TotalRuleDataMap();
        totalRuleDataMap.setRule_data(ruleList);

        return totalRuleDataMap;
    }

    @NonNull
    private List<PojoAnalysisRule> getRuleData() {
        List<PojoAnalysisRule> ruleList = new ArrayList<>();
        SQLiteDatabase db = db_helper.openReadLink();

        String[] columns = {
                BookKeepingColumns.RULE_NAME.toString(),
                BookKeepingColumns.RULE_NO.toString(),
                BookKeepingColumns.TYPE.toString(),
                BookKeepingColumns.PACKAGE_NAME.toString(),
                BookKeepingColumns.NOTIFICATION_TITLE.toString(),
                BookKeepingColumns.NOTIFICATION_CONTENT.toString()
        };
        Cursor rule_cursor = db.query(
                BookKeepingTables.ANALYSIS_RULE.toString(),
                columns,
                null,
                null,
                null,
                null,
                null
        );

        while (rule_cursor.moveToNext()) {
            String ruleName = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.RULE_NAME.toString()));
            long rule_no = rule_cursor.getLong(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.RULE_NO.toString()));
            String type = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.TYPE.toString()));
            String package_name = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.PACKAGE_NAME.toString()));
            String title = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.NOTIFICATION_TITLE.toString()));
            String content = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.NOTIFICATION_CONTENT.toString()));

            PojoAnalysisRule rule = new PojoAnalysisRule(ruleName, rule_no, type, package_name, title, content);
            ruleList.add(rule);
        }

        rule_cursor.close();
        db.close();
        return ruleList;
    }

    private void setRuleData(@NonNull List<PojoAnalysisRule> ruleList) {
        SQLiteDatabase db = db_helper.openWriteLink();

        //清空之前表的内容
        db.delete(BookKeepingTables.ANALYSIS_RULE.toString(), null, null);

        for (PojoAnalysisRule rule : ruleList) {
            String rule_name = rule.getRuleName();
            long rule_no = rule.getRuleNo();
            String type = rule.getType();
            String package_name = rule.getPackageName();
            String title = rule.getTitle();
            String content = rule.getContent();

            ContentValues rule_values = new ContentValues();
            rule_values.put(BookKeepingColumns.RULE_NAME.toString(), rule_name);
            rule_values.put(BookKeepingColumns.RULE_NO.toString(), rule_no);
            rule_values.put(BookKeepingColumns.TYPE.toString(), type);
            rule_values.put(BookKeepingColumns.PACKAGE_NAME.toString(), package_name);
            rule_values.put(BookKeepingColumns.NOTIFICATION_TITLE.toString(), title);
            rule_values.put(BookKeepingColumns.NOTIFICATION_CONTENT.toString(), content);
            db.insert(BookKeepingTables.ANALYSIS_RULE.toString(), null, rule_values);
        }

        db.close();
    }
}
