package com.project.manager.ui.setting.data_io.data_helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.project.manager.data.data_save.database.BookKeepingColumns;
import com.project.manager.data.data_save.database.BookKeepingDatabaseHelper;
import com.project.manager.data.data_save.database.BookKeepingTables;
import com.project.manager.ui.setting.data_io.pojo.PojoAnalysisRule;
import com.project.manager.ui.setting.data_io.maps.TotalRuleDataMap;

import java.util.ArrayList;
import java.util.List;

public class AnalysisRuleDataHelper extends DataHelperBase<BookKeepingDatabaseHelper, TotalRuleDataMap> {
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

        Cursor rule_cursor = db.query(
                BookKeepingTables.ANALYSIS_RULE.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        while (rule_cursor.moveToNext()) {
            String ruleName = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.RULE_NAME.toString()));
            long rule_no = rule_cursor.getLong(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.RULE_NO.toString()));
            long tag_no = rule_cursor.getLong(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NO.toString()));
            String type = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.TYPE.toString()));
            String package_name = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.PACKAGE_NAME.toString()));
            String title = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.NOTIFICATION_TITLE.toString()));
            String content = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.NOTIFICATION_CONTENT.toString()));

            PojoAnalysisRule rule = new PojoAnalysisRule(ruleName, rule_no, tag_no, type, package_name, title, content);
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
            long tag_no = rule.getTag_no();
            String type = rule.getType();
            String package_name = rule.getPackageName();
            String title = rule.getTitle();
            String content = rule.getContent();

            ContentValues rule_values = new ContentValues();
            rule_values.put(BookKeepingColumns.RULE_NAME.toString(), rule_name);
            rule_values.put(BookKeepingColumns.RULE_NO.toString(), rule_no);
            if (isTagNoShouldWrite) {
                rule_values.put(BookKeepingColumns.TAG_NO.toString(), tag_no);
            }
            rule_values.put(BookKeepingColumns.TYPE.toString(), type);
            rule_values.put(BookKeepingColumns.PACKAGE_NAME.toString(), package_name);
            rule_values.put(BookKeepingColumns.NOTIFICATION_TITLE.toString(), title);
            rule_values.put(BookKeepingColumns.NOTIFICATION_CONTENT.toString(), content);
            db.insert(BookKeepingTables.ANALYSIS_RULE.toString(), null, rule_values);
        }

        db.close();
    }

    public static void resetRule(Context context) {
        String tip_str = "数据清除失败：未知原因";
        try {
            BookKeepingDatabaseHelper db_helper = new BookKeepingDatabaseHelper(context);
            SQLiteDatabase db = db_helper.openWriteLink();

            db.delete(BookKeepingTables.ANALYSIS_RULE.toString(), null, null);

            //恢复默认规则
            BookKeepingDatabaseHelper.addDefaultRule(db);

            db.close();
            tip_str = "规则重置成功";
        } catch (SQLiteException e) {
            tip_str = "规则重置失败：数据库异常";
        } finally {
            Toast.makeText(context, tip_str, Toast.LENGTH_SHORT).show();
        }
    }
}
