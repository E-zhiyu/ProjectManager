package com.manager.assistant.data.controllers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import com.manager.assistant.data.classes.AnalysisRule;
import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.Tables;
import com.manager.assistant.auxiliary.enums.AccountType;

import java.util.ArrayList;
import java.util.List;

public class RuleDataController {
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

        Cursor ruleCursor = db.query(
                Tables.ANALYSIS_RULE.toString(),
                null,
                null,
                null,
                null,
                null,
                Columns.RULE_NO.toString()
        );

        while (ruleCursor.moveToNext()) {
            String name = ruleCursor.getString(ruleCursor.getColumnIndexOrThrow(Columns.RULE_NAME.toString()));
            long ruleNo = ruleCursor.getLong(ruleCursor.getColumnIndexOrThrow(Columns.RULE_NO.toString()));
            AccountType type = AccountType.valueOf(ruleCursor.getString(ruleCursor.getColumnIndexOrThrow(Columns.TYPE.toString())));
            String packageName = ruleCursor.getString(ruleCursor.getColumnIndexOrThrow(Columns.PACKAGE_NAME.toString()));
            String title = ruleCursor.getString(ruleCursor.getColumnIndexOrThrow(Columns.NOTIFICATION_TITLE.toString()));
            String content = ruleCursor.getString(ruleCursor.getColumnIndexOrThrow(Columns.NOTIFICATION_CONTENT.toString()));

            AnalysisRule rule = new AnalysisRule(name, ruleNo, type, packageName, title, content);
            ruleList.add(rule);
        }

        ruleCursor.close();
        db.close();
        return ruleList;
    }

    /**
     * 获取转账账户
     *
     * @param ruleNo  通知解析规则编号
     * @param context 上下文
     * @return 带有转出账户和转入账户名称的列表
     */
    @NonNull
    public static List<String> getTransferAccounts(long ruleNo, Context context) {
        List<String> accountList = new ArrayList<>();
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        String[] columns = {
                Columns.EXPORT.toString(),
                Columns.IMPORT.toString()
        };
        String selection = Columns.RULE_NO + "=?";
        String[] selectionArgs = {String.valueOf(ruleNo)};
        Cursor accountCursor = db.query(
                Tables.RULE_ACCOUNT.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1"
        );

        if (accountCursor.moveToNext()) {
            String exportAccount = accountCursor.getString(accountCursor.getColumnIndexOrThrow(Columns.EXPORT.toString()));
            String importAccount = accountCursor.getString(accountCursor.getColumnIndexOrThrow(Columns.IMPORT.toString()));
            accountList.add(exportAccount);
            accountList.add(importAccount);
        }

        accountCursor.close();
        db.close();
        return accountList;
    }

}
