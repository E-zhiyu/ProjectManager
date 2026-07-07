package com.manager.assistant.data.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.Tables;
import com.manager.assistant.generic_enums.KeyStrings;
import com.manager.assistant.auxiliary.enums.AccountType;

public class AccountDataController {
    /**
     * 获取最早的流水日期
     *
     * @param context 上下文
     * @return 最早日期字符串
     * @throws SQLiteException 读取失败引发的数据库异常
     */
    public static String getEarliestAccountDate(Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        String[] columns = {Columns.DATETIME.toString()};
        Cursor basicCursor = db.query(
                Tables.BASIC.toString(),
                columns,
                null,
                null,
                null,
                null,
                Columns.DATETIME.toString()
        );

        String earliestDateStr = "";
        if (basicCursor.moveToFirst()) {
            earliestDateStr = basicCursor.getString(basicCursor.getColumnIndexOrThrow(Columns.DATETIME.toString()));

            //去除后面的时间部分
            earliestDateStr = earliestDateStr.substring(0, 10);
        }

        basicCursor.close();
        db.close();
        return earliestDateStr;
    }

    /**
     * 保存新流水
     *
     * @param dataBundle 新流水数据
     * @param context    上下文
     * @return 新增流水记录的编号
     * @throws SQLiteException 写入数据库可能引发的异常
     */
    public static long saveNewAccount(@NonNull Bundle dataBundle, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //读取数据包的数据
        AccountType type = AccountType.valueOf(dataBundle.getString(KeyStrings.ACCOUNT_TYPE.v()));
        String remark = dataBundle.getString(KeyStrings.ACCOUNT_REMARK.v());
        if (remark == null) remark = "";
        double amount = dataBundle.getDouble(KeyStrings.ACCOUNT_AMOUNT.v(), -1);
        String datetime = dataBundle.getString(KeyStrings.ACCOUNT_DATETIME.v());
        long tagNo = dataBundle.getLong(KeyStrings.TAG_NO.v());

        //生成ContentValues
        ContentValues basicValues = new ContentValues();
        basicValues.put(Columns.TYPE.toString(), type.toString());                     //种类
        basicValues.put(Columns.AMOUNT.toString(), amount);                            //金额
        basicValues.put(Columns.REMARK.toString(), remark);                            //备注
        basicValues.put(Columns.DATETIME.toString(), datetime);                        //日期
        basicValues.put(Columns.TAG_NO.toString(), tagNo);                            //标签编号

        //写入数据
        long rno = db.insert(Tables.BASIC.toString(), null, basicValues);

        //判断是否为特殊类型
        if (type == AccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyStrings.ACCOUNT_EXPORT.v());
            String importAccount = dataBundle.getString(KeyStrings.ACCOUNT_IMPORT.v());

            ContentValues specialValues = new ContentValues();
            specialValues.put(Columns.EXPORT.toString(), exportAccount);
            specialValues.put(Columns.IMPORT.toString(), importAccount);
            specialValues.put(Columns.RNO.toString(), rno);
            db.insert(Tables.TRANSFER.toString(), null, specialValues);
        }

        //更新预算数据
        BudgetDataController.onAccountUpdated(tagNo, tagNo, 0, amount, type, datetime, datetime, db, context);

        db.close();
        return rno;
    }

    /**
     * 删除流水记录
     *
     * @param rno     待删除的流水编号
     * @param context 上下文
     * @throws SQLiteException 写入数据库时可能引发的异常
     */
    public static void deleteAccount(long rno, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //读取旧数据以便修改预算数据
        String[] columns = {
                Columns.AMOUNT.toString(),
                Columns.TAG_NO.toString(),
                Columns.DATETIME.toString(),
                Columns.TYPE.toString()
        };
        String oldSelection = Columns.RNO + "=?";
        String[] oldSelectionArgs = {String.valueOf(rno)};
        Cursor oldDataCursor = db.query(
                Tables.BASIC.toString(),
                columns,
                oldSelection,
                oldSelectionArgs,
                null,
                null,
                null
        );
        long tagNo = 0;
        double amount = 0;
        String datetime = "1970-01-01 00:00";
        AccountType type = AccountType.TRANSFER;
        if (oldDataCursor.moveToFirst()) {
            tagNo = oldDataCursor.getLong(oldDataCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            amount = oldDataCursor.getDouble(oldDataCursor.getColumnIndexOrThrow(Columns.AMOUNT.toString()));
            datetime = oldDataCursor.getString(oldDataCursor.getColumnIndexOrThrow(Columns.DATETIME.toString()));
            type = AccountType.valueOf(oldDataCursor.getString(oldDataCursor.getColumnIndexOrThrow(Columns.TYPE.toString())));
        }
        oldDataCursor.close();

        PictureDataController.deletePicture(rno, db); //删除图片
        deleteTransferAccount(rno, db); //删除转账数据(如果是转账类型)

        String selection = Columns.RNO + "=?";
        String[] selectionArgs = {String.valueOf(rno)};
        db.delete(
                Tables.BASIC.toString(),
                selection,
                selectionArgs
        );

        //更新预算数据
        BudgetDataController.onAccountUpdated(tagNo, tagNo, amount, 0, type, datetime, datetime, db, context);

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

        ContentValues accountValues = new ContentValues();
        accountValues.put(Columns.TAG_NO.toString(), 0);
        db.update(
                Tables.BASIC.toString(),
                accountValues,
                where,
                whereArgs
        );
    }

    /**
     * 清空转账流水记录特有的数据
     *
     * @param rno 流水记录编号
     * @param db  需要修改的数据库
     * @throws SQLiteException 数据库修改失败引发的异常
     */
    public static void deleteTransferAccount(long rno, @NonNull SQLiteDatabase db) throws SQLiteException {
        String where = Columns.RNO + "=?";
        String[] whereArgs = {String.valueOf(rno)};
        db.delete(Tables.TRANSFER.toString(), where, whereArgs);
    }
}
