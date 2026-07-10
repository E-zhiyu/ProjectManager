package com.manager.assistant.data.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.manager.assistant.data.classes.AnalysisRule;
import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.Tables;
import com.manager.assistant.generic_enums.KeyStrings;
import com.manager.assistant.auxiliary.enums.AccountType;

import java.util.ArrayList;
import java.util.HashSet;
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
     * 保存新规则
     *
     * @param newRuleData 新规则的数据包
     * @param context     上下文
     * @return 新规则的编号
     * @throws SQLiteException 写入失败时引发的异常
     */
    public static long saveNewRule(@NonNull Bundle newRuleData, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //解析规则数据
        String ruleName = newRuleData.getString(KeyStrings.ANALYSIS_RULE_NAME.v());
        String type = newRuleData.getString(KeyStrings.ACCOUNT_TYPE.v());
        long tagNo = newRuleData.getLong(KeyStrings.TAG_ID.v());
        String packageName = newRuleData.getString(KeyStrings.PACKAGE_NAME.v());
        String notificationTitle = newRuleData.getString(KeyStrings.NOTIFICATION_TITLE.v());
        String notificationContent = newRuleData.getString(KeyStrings.NOTIFICATION_CONTENT.v());

        //将数据写入数据库
        ContentValues ruleValues = new ContentValues();
        ruleValues.put(Columns.RULE_NAME.toString(), ruleName);
        ruleValues.put(Columns.TYPE.toString(), type);
        ruleValues.put(Columns.TAG_NO.toString(), tagNo);
        ruleValues.put(Columns.PACKAGE_NAME.toString(), packageName);
        ruleValues.put(Columns.NOTIFICATION_TITLE.toString(), notificationTitle);
        ruleValues.put(Columns.NOTIFICATION_CONTENT.toString(), notificationContent);
        long ruleNo = db.insert(Tables.ANALYSIS_RULE.toString(), null, ruleValues);    //获取自增主键值

        //写入转账类型特有的数据
        if (type != null && type.equals(AccountType.TRANSFER.toString())) {
            String exportAccount = newRuleData.getString(KeyStrings.ACCOUNT_EXPORT.v());
            String importAccount = newRuleData.getString(KeyStrings.ACCOUNT_IMPORT.v());

            ContentValues accountValues = new ContentValues();
            accountValues.put(Columns.EXPORT.toString(), exportAccount);
            accountValues.put(Columns.IMPORT.toString(), importAccount);
            accountValues.put(Columns.RULE_NO.toString(), ruleNo);
            db.insert(Tables.RULE_ACCOUNT.toString(), null, accountValues);
        }

        db.close();
        return ruleNo;
    }

    /**
     * 修改规则
     *
     * @param ruleData 包含修改后规则数据的数据包
     * @param context  上下文
     * @throws SQLiteException 写入失败引发的异常
     */
    public static void modifyRule(@NonNull Bundle ruleData, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //解析规则数据
        String ruleName = ruleData.getString(KeyStrings.ANALYSIS_RULE_NAME.v());
        long ruleNo = ruleData.getLong(KeyStrings.ANALYSIS_RULE_NO.v());
        String type = ruleData.getString(KeyStrings.ACCOUNT_TYPE.v());
        long tagNo = ruleData.getLong(KeyStrings.TAG_ID.v());
        String packageName = ruleData.getString(KeyStrings.PACKAGE_NAME.v());
        String notificationTitle = ruleData.getString(KeyStrings.NOTIFICATION_TITLE.v());
        String notificationContent = ruleData.getString(KeyStrings.NOTIFICATION_CONTENT.v());

        String where = Columns.RULE_NO + "=?";
        String[] whereArgs = {String.valueOf(ruleNo)};
        ContentValues ruleValues = new ContentValues();
        ruleValues.put(Columns.RULE_NAME.toString(), ruleName);
        ruleValues.put(Columns.TYPE.toString(), type);
        ruleValues.put(Columns.TAG_NO.toString(), tagNo);
        ruleValues.put(Columns.PACKAGE_NAME.toString(), packageName);
        ruleValues.put(Columns.NOTIFICATION_TITLE.toString(), notificationTitle);
        ruleValues.put(Columns.NOTIFICATION_CONTENT.toString(), notificationContent);
        db.update(Tables.ANALYSIS_RULE.toString(), ruleValues, where, whereArgs);

        //修改记账类型专有的数据
        if (type != null && type.equals(AccountType.TRANSFER.toString())) {
            String exportAccount = ruleData.getString(KeyStrings.ACCOUNT_EXPORT.v());
            String importAccount = ruleData.getString(KeyStrings.ACCOUNT_IMPORT.v());

            ContentValues accountValues = new ContentValues();
            accountValues.put(Columns.EXPORT.toString(), exportAccount);
            accountValues.put(Columns.IMPORT.toString(), importAccount);
            db.update(Tables.RULE_ACCOUNT.toString(), accountValues, where, whereArgs);
        }

        db.close();
    }

    /**
     * 删除规则
     *
     * @param rule_no 需要删除的规则的编号
     * @param context 上下文
     * @throws SQLiteException 写入失败引发的异常
     */
    public static void deleteRule(long rule_no, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        String where = Columns.RULE_NO + "=?";
        String[] whereArgs = {String.valueOf(rule_no)};
        db.delete(Tables.ANALYSIS_RULE.toString(), where, whereArgs);
        db.delete(Tables.RULE_ACCOUNT.toString(), where, whereArgs);

        db.close();
    }

    /**
     * 清除标签
     *
     * @param tag_no 需要清除标签的流水记录对应的标签编号
     * @param db     需要修改的数据库
     * @throws SQLiteException 数据库修改失败引发的异常
     */
    public static void onTagDeleted(long tag_no, @NonNull SQLiteDatabase db) throws SQLiteException {
        String where = Columns.TAG_NO + "=?";
        String[] whereArgs = {String.valueOf(tag_no)};

        ContentValues ruleValues = new ContentValues();
        ruleValues.put(Columns.TAG_NO.toString(), 0);
        db.update(
                Tables.ANALYSIS_RULE.toString(),
                ruleValues,
                where,
                whereArgs
        );
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

    /**
     * 获取所有转出和转入账户
     *
     * @param context 上下文
     * @return 包含所有转出和转入账户名称的哈希集合
     */
    @NonNull
    public static HashSet<String> getAllExportOrImportAccounts(Context context) {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        String[] columns = {
                Columns.EXPORT.toString(),
                Columns.IMPORT.toString()
        };
        Cursor accountCursor = db.query(
                Tables.RULE_ACCOUNT.toString(),
                columns,
                null,
                null,
                null,
                null,
                null
        );

        HashSet<String> accountSet = new HashSet<>();
        while (accountCursor.moveToNext()) {
            String exportAccount = accountCursor.getString(accountCursor.getColumnIndexOrThrow(Columns.EXPORT.toString()));
            String importAccount = accountCursor.getString(accountCursor.getColumnIndexOrThrow(Columns.IMPORT.toString()));
            accountSet.add(exportAccount);
            accountSet.add(importAccount);
        }

        accountCursor.close();
        db.close();
        return accountSet;
    }
}
