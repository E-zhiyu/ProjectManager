package com.project.manager.ui.bookkeeping.auto_bookkeeping.notification_analysis;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import com.project.manager.database.BookKeepingColumns;
import com.project.manager.database.BookKeepingDatabaseHelper;
import com.project.manager.database.BookKeepingTables;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;

import java.util.ArrayList;
import java.util.List;

public class AnalysisRule {
    private final String ruleName;                //规则名称
    private final long ruleNo;                    //规则编号
    private final RunningAccountType type;        //流水种类
    private final long tagNo;                     //流水标签编号
    private final String packageName;             //包名
    private final String notificationTitle;       //通知标题
    private final String notificationContent;     //通知内容

    public AnalysisRule(String ruleName, long ruleNo, RunningAccountType accountType, long tagNo, String packageName, String notificationTitle, String notificationContent) {
        this.ruleName = ruleName;
        this.ruleNo = ruleNo;
        this.type = accountType;
        this.tagNo = tagNo;
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

    public long getTagNo() {
        return tagNo;
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
        BookKeepingDatabaseHelper db_helper = new BookKeepingDatabaseHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();
        List<AnalysisRule> ruleList = new ArrayList<>();

        Cursor rule_cursor = db.query(
                BookKeepingTables.ANALYSIS_RULE.toString(),
                null,
                null,
                null,
                null,
                null,
                BookKeepingColumns.RULE_NO.toString()
        );

        while (rule_cursor.moveToNext()) {
            String rule_name = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.RULE_NAME.toString()));
            long rule_no = rule_cursor.getLong(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.RULE_NO.toString()));
            RunningAccountType type = RunningAccountType.valueOf(rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.TYPE.toString())));
            long tag_no = rule_cursor.getLong(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.TAG_NO.toString()));
            String package_name = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.PACKAGE_NAME.toString()));
            String notification_title = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.NOTIFICATION_TITLE.toString()));
            String notification_content = rule_cursor.getString(rule_cursor.getColumnIndexOrThrow(BookKeepingColumns.NOTIFICATION_CONTENT.toString()));

            AnalysisRule rule = new AnalysisRule(rule_name, rule_no, type, tag_no, package_name, notification_title, notification_content);
            ruleList.add(rule);
        }

        rule_cursor.close();
        db.close();
        return ruleList;
    }
}
