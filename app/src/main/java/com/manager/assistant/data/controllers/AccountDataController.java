package com.manager.assistant.data.controllers;

import android.content.ContentValues;
import android.content.Context;
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
        long tagNo = dataBundle.getLong(KeyStrings.TAG_ID.v());

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
}
