package com.manager.assistant.data.data_class;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.manager.assistant.data.data_save.database.BookkeepingColumns;
import com.manager.assistant.data.data_save.database.BookkeepingDbHelper;
import com.manager.assistant.data.data_save.database.BookkeepingTables;
import com.manager.assistant.enums.KeyValueStrings;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

import java.util.ArrayList;
import java.util.List;

public class AnalysisRule {
    private final String ruleName;                //规则名称
    private final long ruleNo;                    //规则编号
    private final RunningAccountType type;        //流水种类
    private final String packageName;             //包名
    private final String notificationTitle;       //通知标题
    private final String notificationContent;     //通知内容

    public AnalysisRule(String ruleName, long ruleNo, RunningAccountType accountType, String packageName, String notificationTitle, String notificationContent) {
        this.ruleName = ruleName;
        this.ruleNo = ruleNo;
        this.type = accountType;
        this.packageName = packageName;
        this.notificationTitle = notificationTitle;
        this.notificationContent = notificationContent;
    }

    public String getRuleName() {
        return ruleName;
    }

    public long getRuleNo() {
        return ruleNo;
    }

    public RunningAccountType getType() {
        return type;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public String getNotificationContent() {
        return notificationContent;
    }

    /**
     * 读取所有通知解析规则
     *
     * @param context 上下文
     * @return 通知解析规则列表
     * @throws SQLiteException 读取数据库失败引发的异常
     */
    @NonNull
    public static List<AnalysisRule> loadAnalysisRule(Context context) throws SQLiteException {
        BookkeepingDbHelper db_helper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();
        List<AnalysisRule> ruleList = new ArrayList<>();

        Cursor rule_cursor = db.query(
                BookkeepingTables.ANALYSIS_RULE.toString(),
                null,
                null,
                null,
                null,
                null,
                BookkeepingColumns.RULE_NO.toString()
        );

        while (rule_cursor.moveToNext()) {
            String rule_name = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookkeepingColumns.RULE_NAME.toString()));
            long rule_no = rule_cursor.getLong(rule_cursor.getColumnIndexOrThrow(BookkeepingColumns.RULE_NO.toString()));
            RunningAccountType type = RunningAccountType.valueOf(rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookkeepingColumns.TYPE.toString())));
            String package_name = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookkeepingColumns.PACKAGE_NAME.toString()));
            String notification_title = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookkeepingColumns.NOTIFICATION_TITLE.toString()));
            String notification_content = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookkeepingColumns.NOTIFICATION_CONTENT.toString()));

            AnalysisRule rule = new AnalysisRule(rule_name, rule_no, type, package_name, notification_title, notification_content);
            ruleList.add(rule);
        }

        rule_cursor.close();
        db.close();
        return ruleList;
    }

    public static long saveNewRule(@NonNull Bundle newRuleData, Context context) throws SQLiteException {
        BookkeepingDbHelper db_helper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        //解析规则数据
        String rule_name = newRuleData.getString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue());
        String type = newRuleData.getString(KeyValueStrings.ACCOUNT_TYPE.getValue());
        long tag_no = newRuleData.getLong(KeyValueStrings.TAG_NO.getValue());
        String package_name = newRuleData.getString(KeyValueStrings.PACKAGE_NAME.getValue());
        String notification_title = newRuleData.getString(KeyValueStrings.NOTIFICATION_TITLE.getValue());
        String notification_content = newRuleData.getString(KeyValueStrings.NOTIFICATION_CONTENT.getValue());

        //将数据写入数据库
        ContentValues rule_values = new ContentValues();
        rule_values.put(BookkeepingColumns.RULE_NAME.toString(), rule_name);
        rule_values.put(BookkeepingColumns.TYPE.toString(), type);
        rule_values.put(BookkeepingColumns.TAG_NO.toString(), tag_no);
        rule_values.put(BookkeepingColumns.PACKAGE_NAME.toString(), package_name);
        rule_values.put(BookkeepingColumns.NOTIFICATION_TITLE.toString(), notification_title);
        rule_values.put(BookkeepingColumns.NOTIFICATION_CONTENT.toString(), notification_content);
        long rule_no = db.insert(BookkeepingTables.ANALYSIS_RULE.toString(), null, rule_values);    //获取自增主键值

        db.close();
        return rule_no;
    }

    public static void modifyRule(@NonNull Bundle ruleData, Context context) throws SQLiteException {
        BookkeepingDbHelper db_helper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        //解析规则数据
        String rule_name = ruleData.getString(KeyValueStrings.ANALYSIS_RULE_NAME.getValue());
        long rule_no = ruleData.getLong(KeyValueStrings.ANALYSIS_RULE_NO.getValue());
        String type = ruleData.getString(KeyValueStrings.ACCOUNT_TYPE.getValue());
        long tag_no = ruleData.getLong(KeyValueStrings.TAG_NO.getValue());
        String package_name = ruleData.getString(KeyValueStrings.PACKAGE_NAME.getValue());
        String notification_title = ruleData.getString(KeyValueStrings.NOTIFICATION_TITLE.getValue());
        String notification_content = ruleData.getString(KeyValueStrings.NOTIFICATION_CONTENT.getValue());

        String where = BookkeepingColumns.RULE_NO + "=?";
        String[] whereArgs = {String.valueOf(rule_no)};
        ContentValues rule_values = new ContentValues();
        rule_values.put(BookkeepingColumns.RULE_NAME.toString(), rule_name);
        rule_values.put(BookkeepingColumns.TYPE.toString(), type);
        rule_values.put(BookkeepingColumns.TAG_NO.toString(), tag_no);
        rule_values.put(BookkeepingColumns.PACKAGE_NAME.toString(), package_name);
        rule_values.put(BookkeepingColumns.NOTIFICATION_TITLE.toString(), notification_title);
        rule_values.put(BookkeepingColumns.NOTIFICATION_CONTENT.toString(), notification_content);

        db.update(BookkeepingTables.ANALYSIS_RULE.toString(), rule_values, where, whereArgs);

        db.close();
    }

    public static void deleteRule(long rule_no, Context context) throws SQLiteException {
        BookkeepingDbHelper db_helper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        String where = BookkeepingColumns.RULE_NO + "=?";
        String[] whereArgs = {String.valueOf(rule_no)};
        db.delete(BookkeepingTables.ANALYSIS_RULE.toString(), where, whereArgs);

        db.close();
    }

    /**
     * 清除标签
     *
     * @param tag_no 需要清除标签的流水记录对应的标签编号
     * @param db     需要修改的数据库
     * @throws SQLiteException 数据库修改失败引发的异常
     */
    public static void setDefaultTagNo(long tag_no, @NonNull SQLiteDatabase db) throws SQLiteException {
        String where = BookkeepingColumns.TAG_NO + "=?";
        String[] whereArgs = {String.valueOf(tag_no)};

        ContentValues ruleValues = new ContentValues();
        ruleValues.put(BookkeepingColumns.TAG_NO.toString(), 0);
        db.update(
                BookkeepingTables.ANALYSIS_RULE.toString(),
                ruleValues,
                where,
                whereArgs
        );
    }
}
