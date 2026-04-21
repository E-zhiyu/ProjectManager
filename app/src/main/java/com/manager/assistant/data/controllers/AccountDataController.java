package com.manager.assistant.data.controllers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.manager.assistant.data.classes.running_account.ExpenseRunningAccount;
import com.manager.assistant.data.classes.running_account.IncomeRunningAccount;
import com.manager.assistant.data.classes.running_account.RunningAccountBase;
import com.manager.assistant.data.classes.running_account.TransferRunningAccount;
import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.Tables;
import com.manager.assistant.generic_enums.KeyValueStrings;
import com.manager.assistant.ui.others.bottom_sheets.filter.AccountFilterBottomSheet;
import com.manager.assistant.ui.pages.main.bookkeeping.fragments.RunningAccountType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class AccountDataController {
    /**
     * 加载流水记录
     *
     * @param setting    过滤器设置
     * @param searchText 备注搜索文本
     * @param context    上下文
     * @return 包含符合过滤条件的流水记录列表
     */
    @NonNull
    public static List<RunningAccountBase> loadRunningAccountData(
            @NonNull AccountFilterBottomSheet.FilterSetting setting,
            String searchText,
            Context context
    ) {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        //生成selection子句
        StringBuilder selection = new StringBuilder("1=1");
        List<String> selectionArgList = new ArrayList<>();

        //解析过滤器设置
        List<Long> selectedTagList = setting.getSelectedTagList();
        List<Integer> selectedTypeList = setting.getSelectedTypeList();
        LocalDate startDate = setting.getStartDate();
        LocalDate endDate = setting.getEndDate();

        //生成流水种类条件
        if (!selectedTypeList.isEmpty()) {
            selection.append(" AND ");
            selection.append(Columns.TYPE);
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
            selection.append(Columns.TAG_NO);
            selection.append(" IN (");
            selection.append(TextUtils.join(",", Collections.nCopies(selectedTagList.size(), "?")));
            selection.append(")");

            List<String> tagNoStringList = selectedTagList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
            selectionArgList.addAll(tagNoStringList);
        }

        //生成日期条件
        if (startDate != null && endDate != null) {
            selection.append(" AND ");
            selection.append(Columns.DATETIME);
            selection.append(">=?");
            selection.append(" AND ");
            selection.append(Columns.DATETIME);
            selection.append("<?");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String start = formatter.format(startDate);
            String end = formatter.format(endDate.plusDays(1)); //包括最后一天，所以需要第二天的日期
            selectionArgList.add(start);
            selectionArgList.add(end);
        }

        //生成备注搜索条件
        if (searchText != null && !searchText.isEmpty()) {
            selection.append(" AND ");

            //根据空格分割字符串
            String[] keyWords = searchText.trim().split("\\s+");

            //循环生成多个LIKE子句
            int index = 0;
            for (String keyWord : keyWords) {
                //进行转义
                String safeWord = keyWord.replace("/", "//")
                        .replace("%", "/%")
                        .replace("_", "/_");

                selection.append(Columns.REMARK);
                selection.append(" LIKE ? ");
                selection.append("ESCAPE '/'"); //使用“/”进行转义
                selectionArgList.add("%" + safeWord + "%");

                //添加OR
                if (index < keyWords.length - 1) {
                    selection.append(" OR ");
                }

                index++;
            }
        }

        //查询流水记录
        Cursor basicCursor = db.query(
                Tables.BASIC.toString(),
                null,
                selection.toString(),
                selectionArgList.toArray(new String[0]),
                null,
                null,
                Columns.DATETIME + " DESC," + Columns.RNO + " DESC"
        );

        //查询数据
        List<RunningAccountBase> runningAccountList = new ArrayList<>();
        while (basicCursor.moveToNext()) {
            //流水编号
            long rno = basicCursor.getLong(basicCursor.getColumnIndexOrThrow(Columns.RNO.toString()));
            //金额
            double amount = basicCursor.getDouble(basicCursor.getColumnIndexOrThrow(Columns.AMOUNT.toString()));
            //种类
            RunningAccountType type = RunningAccountType.valueOf(basicCursor.getString(basicCursor.getColumnIndexOrThrow(Columns.TYPE.toString())));
            //备注
            String remark = basicCursor.getString(basicCursor.getColumnIndexOrThrow(Columns.REMARK.toString()));
            if (remark == null) remark = "";
            //日期和时间
            String datetime = basicCursor.getString(basicCursor.getColumnIndexOrThrow(Columns.DATETIME.toString()));

            RunningAccountBase runningAccountView = null;
            switch (type) {
                case EXPENSE:
                    runningAccountView = new ExpenseRunningAccount(rno, remark, datetime, amount);
                    break;
                case INCOME:
                    runningAccountView = new IncomeRunningAccount(rno, remark, datetime, amount);
                    break;
                case TRANSFER:
                    String[] columns = {Columns.EXPORT.toString(), Columns.IMPORT.toString()};
                    String transfer_selection = Columns.RNO + "=?";
                    String[] transfer_selectionArgs = {String.valueOf(rno)};

                    Cursor transfer_cursor = db.query(
                            Tables.TRANSFER.toString(),
                            columns,
                            transfer_selection,
                            transfer_selectionArgs,
                            null,
                            null,
                            null
                    );

                    while (transfer_cursor.moveToNext()) {
                        String exportAccount = transfer_cursor.getString(transfer_cursor.getColumnIndexOrThrow(Columns.EXPORT.toString()));
                        String importAccount = transfer_cursor.getString(transfer_cursor.getColumnIndexOrThrow(Columns.IMPORT.toString()));
                        transfer_cursor.close();
                        runningAccountView = new TransferRunningAccount(rno, remark, datetime, amount, exportAccount, importAccount);
                    }
                    break;
            }
            if (runningAccountView != null) {
                runningAccountList.add(runningAccountView);
            }
        }
        basicCursor.close();
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
        BookkeepingDbHelper db_helper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = db_helper.openReadLink();

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
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String datetime = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        long tagNo = dataBundle.getLong(KeyValueStrings.TAG_NO.getValue());

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
        if (type == RunningAccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());

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
     * 修改流水记录
     *
     * @param dataBundle 修改后的流水记录数据
     * @param context    上下文
     * @throws SQLiteException 写入数据库可能引发的异常
     */
    public static void modifyAccount(@NonNull Bundle dataBundle, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //解析数据
        long rno = dataBundle.getLong(KeyValueStrings.ACCOUNT_NO.getValue());
        RunningAccountType type = RunningAccountType.valueOf(dataBundle.getString(KeyValueStrings.ACCOUNT_TYPE.getValue()));
        double amount = dataBundle.getDouble(KeyValueStrings.ACCOUNT_AMOUNT.getValue(), -1);
        String remark = dataBundle.getString(KeyValueStrings.ACCOUNT_REMARK.getValue());
        if (remark == null) remark = "";
        String datetime = dataBundle.getString(KeyValueStrings.ACCOUNT_DATETIME.getValue());
        long tagNo = dataBundle.getLong(KeyValueStrings.TAG_NO.getValue());

        //读取旧数据以便修改预算数据
        String[] columns = {
                Columns.AMOUNT.toString(),
                Columns.TAG_NO.toString(),
                Columns.DATETIME.toString()
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
        long oldTagNo = tagNo;
        double oldAmount = amount;
        String oldDatetime = datetime;
        if (oldDataCursor.moveToFirst()) {
            oldTagNo = oldDataCursor.getLong(oldDataCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            oldAmount = oldDataCursor.getDouble(oldDataCursor.getColumnIndexOrThrow(Columns.AMOUNT.toString()));
            oldDatetime = oldDataCursor.getString(oldDataCursor.getColumnIndexOrThrow(Columns.DATETIME.toString()));
        }
        oldDataCursor.close();

        //修改基本数据
        ContentValues basicValues = new ContentValues();
        basicValues.put(Columns.TYPE.toString(), type.toString());  //种类
        basicValues.put(Columns.AMOUNT.toString(), amount);         //金额
        basicValues.put(Columns.REMARK.toString(), remark);         //备注
        basicValues.put(Columns.DATETIME.toString(), datetime);    //日期
        basicValues.put(Columns.TAG_NO.toString(), tagNo);         //标签编号
        String selection = Columns.RNO + "=?";
        String[] selectionArgs = new String[]{String.valueOf(rno)};
        db.update(
                Tables.BASIC.toString(),
                basicValues,
                selection,
                selectionArgs
        );

        //修改特殊数据
        ContentValues specialValues = new ContentValues();
        if (type == RunningAccountType.TRANSFER) {
            String exportAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_EXPORT.getValue());
            String importAccount = dataBundle.getString(KeyValueStrings.ACCOUNT_IMPORT.getValue());

            specialValues.put(Columns.EXPORT.toString(), exportAccount);
            specialValues.put(Columns.IMPORT.toString(), importAccount);
            db.update(
                    Tables.TRANSFER.toString(),
                    specialValues,
                    selection,
                    selectionArgs
            );
        }

        //修改预算数据
        BudgetDataController.onAccountUpdated(oldTagNo, tagNo, oldAmount, amount, type, oldDatetime, datetime, db, context);

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
        RunningAccountType type = RunningAccountType.TRANSFER;
        if (oldDataCursor.moveToFirst()) {
            tagNo = oldDataCursor.getLong(oldDataCursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            amount = oldDataCursor.getDouble(oldDataCursor.getColumnIndexOrThrow(Columns.AMOUNT.toString()));
            datetime = oldDataCursor.getString(oldDataCursor.getColumnIndexOrThrow(Columns.DATETIME.toString()));
            type = RunningAccountType.valueOf(oldDataCursor.getString(oldDataCursor.getColumnIndexOrThrow(Columns.TYPE.toString())));
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

    /**
     * 获取所有转出账户和转入账户的名称
     *
     * @param context 上下文
     * @return 包含所有转出账户和转入账户名称的列表
     * @throws SQLiteException 读取失败引发的异常
     */
    @NonNull
    public static HashSet<String> getAllExportOrImportAccounts(Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        String[] columns = {
                Columns.EXPORT.toString(),
                Columns.IMPORT.toString()
        };
        Cursor transferCursor = db.query(
                Tables.TRANSFER.toString(),
                columns,
                null,
                null,
                null,
                null,
                null,
                null
        );

        HashSet<String> nameSet = new HashSet<>();
        while (transferCursor.moveToNext()) {
            String exportAccountName = transferCursor.getString(transferCursor.getColumnIndexOrThrow(Columns.EXPORT.toString()));
            String importAccountName = transferCursor.getString(transferCursor.getColumnIndexOrThrow(Columns.IMPORT.toString()));

            nameSet.add(exportAccountName);
            nameSet.add(importAccountName);
        }

        transferCursor.close();
        db.close();
        return nameSet;
    }
}
