package com.project.manager.data.data_class.running_account;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.project.manager.data.data_class.Picture;
import com.project.manager.data.data_save.database.BookKeepingColumns;
import com.project.manager.data.data_save.database.BookKeepingDbHelper;
import com.project.manager.data.data_save.database.BookKeepingTables;
import com.project.manager.ui.pages.bookkeeping.KeyValueStrings;
import com.project.manager.ui.pages.bookkeeping.running_account_edit.fragments.RunningAccountType;

public abstract class RunningAccountBase {
    protected String name;              //名称
    protected RunningAccountType type;  //种类
    protected String remark;            //备注
    protected boolean isDefaultRemark;  //是否使用默认备注
    protected String date_time;         //日期和时间
    protected double amount;            //金额
    protected long rno;                 //流水编号

    public String getName() {
        return name;
    }

    public RunningAccountType getType() {
        return type;
    }

    public String getRemark() {
        return remark;
    }

    public boolean isDefaultRemark() {
        return isDefaultRemark;
    }

    public String getDate_time() {
        return date_time;
    }

    public double getAmount() {
        return amount;
    }

    public long getRno() {
        return rno;
    }

    public void setRno(long rno) {
        this.rno = rno;
    }

    /**
     * 获取最早的流水日期
     *
     * @param context 上下文
     * @return 最早日期字符串
     * @throws SQLiteException 读取失败引发的数据库异常
     */
    public static String getEarliestAccountDate(Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();

        String[] columns = {BookKeepingColumns.DATETIME.toString()};
        Cursor basic_cursor = db.query(
                BookKeepingTables.BASIC.toString(),
                columns,
                null,
                null,
                null,
                null,
                BookKeepingColumns.DATETIME.toString(),
                "1"
        );

        String earliest_date_str = "";
        if (basic_cursor.moveToNext()) {
            earliest_date_str = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.DATETIME.toString()));

            //去除后面的时间部分
            earliest_date_str = earliest_date_str.substring(0, 10);
        }

        basic_cursor.close();
        db.close();
        return earliest_date_str;
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
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        String type = dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue());
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        boolean isDefaultRemark = dataBundle.getBoolean(KeyValueStrings.ACCOUNT_IS_DEFAULT_REMARK.getValue());
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        long tag_no = dataBundle.getLong(KeyValueStrings.TAG_NO.getValue());

        ContentValues basic_values = new ContentValues();
        basic_values.put(BookKeepingColumns.TYPE.toString(), type);                                 //种类
        basic_values.put(BookKeepingColumns.AMOUNT.toString(), amount);                             //金额
        basic_values.put(BookKeepingColumns.REMARK.toString(), isDefaultRemark ? null : remark);    //备注
        basic_values.put(BookKeepingColumns.DATETIME.toString(), date_time);                        //日期
        basic_values.put(BookKeepingColumns.TAG_NO.toString(), tag_no);                             //标签编号

        long rno = db.insert(BookKeepingTables.BASIC.toString(), null, basic_values);

        //判断是否为特殊类型
        ContentValues special_values = new ContentValues();
        if (type != null && type.equals(RunningAccountType.TRANSFER.toString())) {
            String export_account = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());
            String import_account = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());

            special_values.put(BookKeepingColumns.EXPORT.toString(), export_account);
            special_values.put(BookKeepingColumns.IMPORT.toString(), import_account);
            special_values.put(BookKeepingColumns.RNO.toString(), rno);
            db.insert(BookKeepingTables.TRANSFER.toString(), null, special_values);
        }

        db.close();
        return rno;
    }

    /**
     * 修改流水记录
     *
     * @param dataBundle 修改后的流水记录数据
     * @param context    上下文
     * @throws SQLiteException 写入数据库可能引发的异常
     */
    public static void modifyAccount(@NonNull Bundle dataBundle, Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        //解析数据
        long rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());
        String type = dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue());
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        boolean isDefaultRemark = dataBundle.getBoolean(KeyValueStrings.ACCOUNT_IS_DEFAULT_REMARK.getValue());
        String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        long tag_no = dataBundle.getLong(KeyValueStrings.TAG_NO.getValue());

        //修改基本数据
        ContentValues basic_values = new ContentValues();
        basic_values.put(BookKeepingColumns.TYPE.toString(), type);                   //种类
        basic_values.put(BookKeepingColumns.AMOUNT.toString(), amount);                          //金额
        basic_values.put(BookKeepingColumns.REMARK.toString(), isDefaultRemark ? null : remark); //备注
        basic_values.put(BookKeepingColumns.DATETIME.toString(), date_time);                     //日期
        basic_values.put(BookKeepingColumns.TAG_NO.toString(), tag_no);                          //标签编号
        String selection = BookKeepingColumns.RNO + "=?";
        String[] selectionArgs = new String[]{String.valueOf(rno)};
        db.update(
                BookKeepingTables.BASIC.toString(),
                basic_values,
                selection,
                selectionArgs
        );

        //修改特殊数据
        ContentValues special_values = new ContentValues();
        if (type != null && type.equals(RunningAccountType.TRANSFER.toString())) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());

            special_values.put(BookKeepingColumns.EXPORT.toString(), exportAccount);
            special_values.put(BookKeepingColumns.IMPORT.toString(), importAccount);
            db.update(
                    BookKeepingTables.TRANSFER.toString(),
                    special_values,
                    selection,
                    selectionArgs
            );
        }

        db.close();
    }

    /**
     * 删除流水记录
     *
     * @param rno     待删除的流水编号
     * @param context 上下文
     * @throws SQLiteException 写入数据库时可能引发的异常
     */
    public static void deleteAccount(long rno, Context context) throws SQLiteException {
        BookKeepingDbHelper db_helper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openWriteLink();

        Picture.deletePicture(rno, db); //删除图片
        TransferRunningAccount.deleteTransferAccount(rno, db);  //删除转账数据(如果是转账类型)

        String selection = BookKeepingColumns.RNO + "=?";
        String[] selectionArgs = {String.valueOf(rno)};
        db.delete(
                BookKeepingTables.BASIC.toString(),
                selection,
                selectionArgs
        );

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
        String where = BookKeepingColumns.TAG_NO + "=?";
        String[] whereArgs = {String.valueOf(tag_no)};

        ContentValues accountValues = new ContentValues();
        accountValues.put(BookKeepingColumns.TAG_NO.toString(), 0);
        db.update(
                BookKeepingTables.BASIC.toString(),
                accountValues,
                where,
                whereArgs
        );
    }
}

