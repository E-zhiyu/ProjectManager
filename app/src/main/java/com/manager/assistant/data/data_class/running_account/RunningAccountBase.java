package com.manager.assistant.data.data_class.running_account;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.manager.assistant.data.data_class.Picture;
import com.manager.assistant.data.data_save.database.BookKeepingColumns;
import com.manager.assistant.data.data_save.database.BookKeepingDbHelper;
import com.manager.assistant.data.data_save.database.BookKeepingTables;
import com.manager.assistant.enums.KeyValueStrings;
import com.manager.assistant.ui.others.bottom_sheets.filter.AccountFilterBottomSheet;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public abstract class RunningAccountBase {
    protected String title;              //名称
    protected RunningAccountType type;  //种类
    protected String remark;            //备注
    protected String defaultRemark;     //默认备注
    protected String datetime;          //日期和时间
    protected double amount;            //金额
    protected long rno;                 //流水编号

    public RunningAccountBase() {
        defaultRemark = initDefaultRemark();
    }

    public String getTitle() {
        return title;
    }

    public RunningAccountType getType() {
        return type;
    }

    public String getRemark() {
        return remark == null ? "" : remark;
    }

    public String getDefaultRemark() {
        return defaultRemark;
    }

    public String getDatetime() {
        return datetime;
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

    protected abstract String initDefaultRemark();

    /**
     * 加载流水记录
     *
     * @param setting 过滤器设置
     * @param context 上下文
     * @return 包含符合过滤条件的流水记录列表
     */
    @NonNull
    public static List<RunningAccountBase> loadRunningAccountData(@NonNull AccountFilterBottomSheet.FilterSetting setting, Context context) {
        BookKeepingDbHelper dbHelper = new BookKeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        //生成selection子句
        StringBuilder selection = new StringBuilder("1=1");
        List<String> selectionArgList = new ArrayList<>();
        List<Long> selectedTagList = setting.getSelectedTagList();
        List<Integer> selectedTypeList = setting.getSelectedTypeList();
        Calendar startCalendar = setting.getStartCalendar();
        Calendar endCalendar = setting.getEndCalendar();

        //生成流水种类条件
        if (!selectedTypeList.isEmpty()) {
            selection.append(" AND ");
            selection.append(BookKeepingColumns.TYPE);
            selection.append(" IN (");
            selection.append(TextUtils.join(",", Collections.nCopies(selectedTypeList.size(), "?")));
            selection.append(")");

            List<String> typeStringList = selectedTypeList.stream()
                    .map(ordinal -> RunningAccountType.values()[ordinal])
                    .map(RunningAccountType::toString)
                    .collect(Collectors.toList());
            selectionArgList.addAll(typeStringList);
        }

        //生成标签条件
        if (!selectedTagList.isEmpty()) {
            selection.append(" AND ");
            selection.append(BookKeepingColumns.TAG_NO);
            selection.append(" IN (");
            selection.append(TextUtils.join(",", Collections.nCopies(selectedTagList.size(), "?")));
            selection.append(")");

            List<String> tagNoStringList = selectedTagList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
            selectionArgList.addAll(tagNoStringList);
        }

        //生成日期条件
        if (startCalendar != null && endCalendar != null) {
            selection.append(" AND ");
            selection.append(BookKeepingColumns.DATETIME);
            selection.append(">=?");
            selection.append(" AND ");
            selection.append(BookKeepingColumns.DATETIME);
            selection.append("<?");

            int sy = startCalendar.get(Calendar.YEAR);
            int sm = startCalendar.get(Calendar.MONTH) + 1;
            int sd = startCalendar.get(Calendar.DAY_OF_MONTH);
            int ey = endCalendar.get(Calendar.YEAR);
            int em = endCalendar.get(Calendar.MONTH) + 1;
            int ed = endCalendar.get(Calendar.DAY_OF_MONTH) + 1;    //包含最后一天，所以加一
            selectionArgList.add(String.format(Locale.getDefault(), "%04d-%02d-%02d", sy, sm, sd));
            selectionArgList.add(String.format(Locale.getDefault(), "%04d-%02d-%02d", ey, em, ed));
        }

        //查询流水记录
        Cursor basic_cursor = db.query(
                BookKeepingTables.BASIC.toString(),
                null,
                selection.toString(),
                selectionArgList.toArray(new String[0]),
                null,
                null,
                BookKeepingColumns.DATETIME + " DESC," + BookKeepingColumns.RNO + " DESC"
        );

        //查询数据
        List<RunningAccountBase> runningAccountList = new ArrayList<>();
        while (basic_cursor.moveToNext()) {
            //流水编号
            long rno = basic_cursor.getLong(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.RNO.toString()));
            //金额
            double amount = basic_cursor.getDouble(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.AMOUNT.toString()));
            //种类
            RunningAccountType type = RunningAccountType.valueOf(basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.TYPE.toString())));
            //备注
            String remark = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.REMARK.toString()));
            if (remark == null) remark = "";
            //日期和时间
            String datetime = basic_cursor.getString(basic_cursor.getColumnIndexOrThrow(BookKeepingColumns.DATETIME.toString()));

            RunningAccountBase runningAccountView = null;
            switch (type) {
                case EXPENSE:
                    runningAccountView = new ExpenseRunningAccount(rno, remark, datetime, amount);
                    break;
                case INCOME:
                    runningAccountView = new IncomeRunningAccount(rno, remark, datetime, amount);
                    break;
                case TRANSFER:
                    String[] columns = {BookKeepingColumns.EXPORT.toString(), BookKeepingColumns.IMPORT.toString()};
                    String transfer_selection = BookKeepingColumns.RNO + "=?";
                    String[] transfer_selectionArgs = {String.valueOf(rno)};

                    Cursor transfer_cursor = db.query(
                            BookKeepingTables.TRANSFER.toString(),
                            columns,
                            transfer_selection,
                            transfer_selectionArgs,
                            null,
                            null,
                            null
                    );

                    while (transfer_cursor.moveToNext()) {
                        String exportAccount = transfer_cursor.getString(transfer_cursor.getColumnIndexOrThrow(BookKeepingColumns.EXPORT.toString()));
                        String importAccount = transfer_cursor.getString(transfer_cursor.getColumnIndexOrThrow(BookKeepingColumns.IMPORT.toString()));
                        transfer_cursor.close();
                        runningAccountView = new TransferRunningAccount(rno, remark, datetime, amount, exportAccount, importAccount);
                    }
                    break;
            }
            if (runningAccountView != null) {
                runningAccountList.add(runningAccountView);
            }
        }
        basic_cursor.close();
        db.close();

        return runningAccountList;
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
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        long tag_no = dataBundle.getLong(KeyValueStrings.TAG_NO.getValue());

        ContentValues basic_values = new ContentValues();
        basic_values.put(BookKeepingColumns.TYPE.toString(), type);                                 //种类
        basic_values.put(BookKeepingColumns.AMOUNT.toString(), amount);                             //金额
        basic_values.put(BookKeepingColumns.REMARK.toString(), remark);                             //备注
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
        String date_time = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        long tag_no = dataBundle.getLong(KeyValueStrings.TAG_NO.getValue());

        //修改基本数据
        ContentValues basic_values = new ContentValues();
        basic_values.put(BookKeepingColumns.TYPE.toString(), type);             //种类
        basic_values.put(BookKeepingColumns.AMOUNT.toString(), amount);         //金额
        basic_values.put(BookKeepingColumns.REMARK.toString(), remark);         //备注
        basic_values.put(BookKeepingColumns.DATETIME.toString(), date_time);    //日期
        basic_values.put(BookKeepingColumns.TAG_NO.toString(), tag_no);         //标签编号
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

