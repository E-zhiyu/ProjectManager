package com.manager.assistant.data.controllers;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.manager.assistant.R;
import com.manager.assistant.data.classes.Budget;
import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.Tables;
import com.manager.assistant.generic_enums.ChannelInfo;
import com.manager.assistant.generic_enums.NotificationID;
import com.manager.assistant.generic_enums.PendingRequestCode;
import com.manager.assistant.helpers.NotificationHelper;
import com.manager.assistant.ui.pages.bookkeeping.budget.BudgetManageActivity;
import com.manager.assistant.ui.pages.bookkeeping.budget.ResetFrequency;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BudgetDataController {
    /**
     * 获取待更新的预算的编号
     *
     * @param db       数据库实例
     * @param tagNo    流水记录对应的标签编号
     * @param datetime 流水记录的日期
     * @return 预算编号列表
     * @throws SQLiteException 读取失败引发的异常
     */
    @NonNull
    private static List<Long> getBudgetsNeedToUpdate(
            SQLiteDatabase db,
            long tagNo,
            @NonNull String datetime
    ) throws SQLiteException {
        //将 datetime 转换为 date
        if (datetime.length() > 10) {
            datetime = datetime.substring(0, 10);
        }

        //生成 SQL 语句
        String sql = "SELECT " + String.format(Locale.getDefault(), "%s.%s", Tables.BUDGET_TAG, Columns.BNO) +
                " FROM " + Tables.BUDGET_TAG +
                " INNER JOIN " + Tables.BUDGET +
                " ON " + String.format(Locale.getDefault(), "%s.%s", Tables.BUDGET_TAG, Columns.BNO) +
                "=" + String.format(Locale.getDefault(), "%s.%s", Tables.BUDGET, Columns.BNO) +
                " WHERE " + Columns.START_DATE + "<=?" +
                " AND " + Columns.TAG_NO + "=?";
        String[] sqlArgs = {
                datetime,
                String.valueOf(tagNo)
        };

        //获取需要重置的预算编号
        List<Long> bnoList = new ArrayList<>();
        Cursor budgetCursor = db.rawQuery(sql, sqlArgs);
        while (budgetCursor.moveToNext()) {
            long bno = budgetCursor.getLong(budgetCursor.getColumnIndexOrThrow(String.format(Locale.getDefault(), "%s.%s", Tables.BUDGET_TAG, Columns.BNO)));
            bnoList.add(bno);
        }

        budgetCursor.close();
        return bnoList;
    }

    /**
     * 通过预算编号读取标签编号
     *
     * @param db  数据库实例
     * @param bno 预算编号
     * @return 该预算编号对应的所有标签编号的列表
     * @throws SQLiteException 读取失败引发的异常
     */
    @NonNull
    private static List<Long> getTagNoByBno(
            @NonNull SQLiteDatabase db,
            long bno
    ) throws SQLiteException {
        String[] columns = {
                Columns.TAG_NO.toString()
        };
        String selection = Columns.BNO + "=?";
        String[] selectionArgs = {String.valueOf(bno)};
        Cursor cursor = db.query(
                Tables.BUDGET_TAG.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        List<Long> tagNoList = new ArrayList<>();
        while (cursor.moveToNext()) {
            long tagNo = cursor.getLong(cursor.getColumnIndexOrThrow(Columns.TAG_NO.toString()));
            tagNoList.add(tagNo);
        }

        cursor.close();
        return tagNoList;
    }

    /**
     * 将标签数据写入预算-标签表
     *
     * @param db        需要写入的数据库
     * @param bno       预算编号
     * @param tagNoList 标签编号列表
     * @throws SQLiteException 写入失败引发的异常
     */
    private static void saveTagNoWithBno(
            SQLiteDatabase db,
            long bno,
            @NonNull List<Long> tagNoList
    ) throws SQLiteException {
        ContentValues values = new ContentValues();
        for (long tagNo : tagNoList) {
            values.put(Columns.TAG_NO.toString(), tagNo);
            values.put(Columns.BNO.toString(), bno);
            db.insert(Tables.BUDGET_TAG.toString(), null, values);

            values.clear();
        }
    }

    /**
     * 通过预算编号修改标签数据
     *
     * @param db        需要写入数据的数据库
     * @param bno       预算编号
     * @param tagNoList 修改后的标签编号列表
     * @throws SQLiteException 写入失败引发的异常
     */
    private static void modifyTagNoWithBno(
            @NonNull SQLiteDatabase db,
            long bno,
            @NonNull List<Long> tagNoList
    ) throws SQLiteException {
        //移除旧数据
        deleteTagNoWithBno(db, bno);

        //写入新数据
        saveTagNoWithBno(db, bno, tagNoList);
    }

    /**
     * 通过预算编号删除标签数据
     *
     * @param db  写入数据的数据库
     * @param bno 需要删除的预算编号
     * @throws SQLiteException 写入失败引发的异常
     */
    private static void deleteTagNoWithBno(
            @NonNull SQLiteDatabase db,
            long bno
    ) throws SQLiteException {
        String where = Columns.BNO + "=?";
        String[] whereArgs = {String.valueOf(bno)};

        db.delete(Tables.BUDGET_TAG.toString(), where, whereArgs);
    }

    /**
     * 读取所有预算
     *
     * @param context 上下文
     * @return 包含所有预算的列表
     * @throws SQLiteException 读取失败引发的异常
     */
    @NonNull
    public static List<Budget> getAllBudgets(Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        Cursor budgetCursor = db.query(
                Tables.BUDGET.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        List<Budget> budgetList = new ArrayList<>();
        while (budgetCursor.moveToNext()) {
            long bno = budgetCursor.getLong(budgetCursor.getColumnIndexOrThrow(Columns.BNO.toString()));
            String name = budgetCursor.getString(budgetCursor.getColumnIndexOrThrow(Columns.BUDGET_NAME.toString()));
            double initAmount = budgetCursor.getDouble(budgetCursor.getColumnIndexOrThrow(Columns.INIT_AMOUNT.toString()));
            double leftAmount = budgetCursor.getDouble(budgetCursor.getColumnIndexOrThrow(Columns.LEFT_AMOUNT.toString()));
            String startDate = budgetCursor.getString(budgetCursor.getColumnIndexOrThrow(Columns.START_DATE.toString()));
            String resetFrequencyStr = budgetCursor.getString(budgetCursor.getColumnIndexOrThrow(Columns.RESET_FREQUENCY.toString()));
            ResetFrequency resetFrequency = ResetFrequency.valueOf(resetFrequencyStr);
            List<Long> tagNoList = getTagNoByBno(db, bno);

            Budget budget = new Budget(bno, name, initAmount, leftAmount, startDate, resetFrequency, tagNoList);
            budgetList.add(budget);
        }

        budgetCursor.close();
        db.close();
        return budgetList;
    }

    /**
     * 添加新的预算
     *
     * @param budget  预算实例
     * @param context 上下文
     * @return 为新预算分配的编号
     * @throws SQLiteException 写入失败引发的异常
     */
    public static long saveNewBudget(@NonNull Budget budget, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //解析数据
        String name = budget.getName();
        double initAmount = budget.getInitAmount();
        double leftAmount = budget.getLeftAmount();
        String startDate = budget.getStartDate();
        ResetFrequency resetFrequency = budget.getResetFrequency();
        List<Long> tagNoList = budget.getTagNoList();

        //写入数据
        ContentValues budgetValues = new ContentValues();
        budgetValues.put(Columns.BUDGET_NAME.toString(), name);
        budgetValues.put(Columns.INIT_AMOUNT.toString(), initAmount);
        budgetValues.put(Columns.LEFT_AMOUNT.toString(), leftAmount);
        budgetValues.put(Columns.START_DATE.toString(), startDate);
        budgetValues.put(Columns.RESET_FREQUENCY.toString(), resetFrequency.toString());
        long bno = db.insert(Tables.BUDGET.toString(), null, budgetValues);
        saveTagNoWithBno(db, bno, tagNoList);

        db.close();
        return bno;
    }

    /**
     * 修改预算
     *
     * @param budget  修改后的预算实例
     * @param context 上下文
     * @throws SQLiteException 写入失败引发的异常
     */
    public static void modifyBudget(@NonNull Budget budget, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //解析数据
        long bno = budget.getBno();
        String name = budget.getName();
        double initAmount = budget.getInitAmount();
        double leftAmount = budget.getLeftAmount();
        String startDate = budget.getStartDate();
        ResetFrequency resetFrequency = budget.getResetFrequency();
        List<Long> tagNoList = budget.getTagNoList();

        //写入数据
        String where = Columns.BNO + "=?";
        String[] whereArgs = {String.valueOf(bno)};
        ContentValues budgetValues = new ContentValues();
        budgetValues.put(Columns.BUDGET_NAME.toString(), name);
        budgetValues.put(Columns.INIT_AMOUNT.toString(), initAmount);
        budgetValues.put(Columns.LEFT_AMOUNT.toString(), leftAmount);
        budgetValues.put(Columns.START_DATE.toString(), startDate);
        budgetValues.put(Columns.RESET_FREQUENCY.toString(), resetFrequency.toString());
        db.update(Tables.BUDGET.toString(), budgetValues, where, whereArgs);
        modifyTagNoWithBno(db, bno, tagNoList);

        db.close();
    }

    /**
     * 删除预算
     *
     * @param bno     待删除的预算的编号
     * @param context 上下文
     * @throws SQLiteException 写入数据失败引发的异常
     */
    public static void deleteBudget(long bno, Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        String where = Columns.BNO + "=?";
        String[] whereArgs = {String.valueOf(bno)};
        deleteTagNoWithBno(db, bno);
        db.delete(Tables.BUDGET.toString(), where, whereArgs);

        db.close();
    }

    /**
     * 预算余额更新后检查余额是否低于初始金额的10%，若低于10%则发送通知提醒用户
     *
     * @param bnoList 更新了余额的预算编号
     * @param db      可读数据库实例
     * @param context 上下文
     * @throws SQLiteException 数据读取失败引发的异常
     */
    private static void checkLeftAmountThreshold(@NonNull List<Long> bnoList, @NonNull SQLiteDatabase db, Context context) throws SQLiteException {
        //生成查询条件
        String selection = Columns.BNO + " IN (" +
                TextUtils.join(",", Collections.nCopies(bnoList.size(), "?")) +
                ")";
        String[] selectionArgs = bnoList.stream()
                .map(String::valueOf)
                .toArray(String[]::new);
        String[] columns = {
                Columns.BUDGET_NAME.toString(),
                Columns.INIT_AMOUNT.toString(),
                Columns.LEFT_AMOUNT.toString()
        };

        //获取游标
        Cursor budgetCursor = db.query(
                Tables.BUDGET.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        //读取数据
        List<String> budgetNameList = new ArrayList<>();
        while (budgetCursor.moveToNext()) {
            String name = budgetCursor.getString(budgetCursor.getColumnIndexOrThrow(Columns.BUDGET_NAME.toString()));
            double initAmount = budgetCursor.getDouble(budgetCursor.getColumnIndexOrThrow(Columns.INIT_AMOUNT.toString()));
            double leftAmount = budgetCursor.getDouble(budgetCursor.getColumnIndexOrThrow(Columns.LEFT_AMOUNT.toString()));

            if (leftAmount <= initAmount * 0.1) {
                budgetNameList.add(name);
            }
        }
        budgetCursor.close();

        //生成通知内容并发送通知
        StringBuilder content = new StringBuilder();
        if (budgetNameList.size() == 1) {
            content.append(budgetNameList.get(0));
        } else if (budgetNameList.size() == 2) {
            content.append(String.format(
                    Locale.getDefault(),
                    "%s和%s",
                    budgetNameList.get(0),
                    budgetNameList.get(1)
            ));
        } else if (budgetNameList.size() == 3) {
            content.append(String.format(
                    Locale.getDefault(),
                    "%s、%s和%s",
                    budgetNameList.get(0),
                    budgetNameList.get(1),
                    budgetNameList.get(2)
            ));
        } else if (budgetNameList.size() > 3) {
            content.append(String.format(
                    Locale.getDefault(),
                    "%s、%s、%s……共计%d个预算",
                    budgetNameList.get(0),
                    budgetNameList.get(1),
                    budgetNameList.get(2),
                    budgetNameList.size()
            ));
        }
        if (content.length() > 0) {
            content.append("的余额已不足10%，请注意查看。");

            //设置点击意图
            Intent intent = new Intent(context, BudgetManageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    PendingRequestCode.BUDGET_NOTIFICATION.ordinal(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            //发送通知
            String channelID = ChannelInfo.BUDGET_AMOUNT.getId();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("预算余额提醒")
                    .setContentText(content.toString())
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(true);
            NotificationHelper.sendNotification(NotificationID.BUDGET_AMOUNT_WARNING.ordinal(), builder, context);
        }
    }

    /**
     * 增加剩余金额
     *
     * @param tagNo        流水记录的标签编号
     * @param amountChange 剩余金额增加的值(可以为负数)
     * @param datetime     流水记录的日期
     * @param db           需要写入数据的数据库实例
     * @param context      上下文
     * @throws SQLiteException 数据写入失败引发的异常
     */
    private static void increaseLeftAmount(
            long tagNo,
            double amountChange,
            @NonNull String datetime,
            SQLiteDatabase db,
            Context context
    ) throws SQLiteException {
        if (amountChange == 0) return;

        //获取需要更新的预算编号列表
        List<Long> bnoList = getBudgetsNeedToUpdate(db, tagNo, datetime);
        if (bnoList.isEmpty()) {
            return;
        }

        //生成 SQL 语句
        String sql = "UPDATE " + Tables.BUDGET + " SET " + Columns.LEFT_AMOUNT + "=" +
                "MAX(0," + "MIN(" + Columns.LEFT_AMOUNT + "+?" + "," + Columns.INIT_AMOUNT + ")" + ")" +
                " WHERE " + Columns.BNO + " IN (" +
                TextUtils.join(",", Collections.nCopies(bnoList.size(), "?")) +
                ")";
        SQLiteStatement statement = db.compileStatement(sql);

        //绑定 SQL 语句的变量
        statement.bindDouble(1, amountChange);
        for (int index = 0; index < bnoList.size(); index++) {
            statement.bindLong(index + 2, bnoList.get(index));
        }

        //执行 SQL 语句
        statement.executeUpdateDelete();

        //余额修改完成后检查是否低于阈值
        checkLeftAmountThreshold(bnoList, db, context);
    }

    /**
     * 减少剩余金额
     *
     * @param tagNo        流水记录的标签编号
     * @param amountChange 剩余金额减少的值(可以为负数)
     * @param datetime     流水记录的日期
     * @param db           需要写入数据的数据库实例
     * @param context      上下文
     * @throws SQLiteException 数据写入失败引发的异常
     */
    private static void decreaseLeftAmount(
            long tagNo,
            double amountChange,
            String datetime,
            SQLiteDatabase db,
            Context context
    ) throws SQLiteException {
        increaseLeftAmount(tagNo, -amountChange, datetime, db, context);
    }

    /**
     * 处理流水记录更新的方法
     *
     * @param oldTagNo    原本的流水记录标签编号
     * @param tagNo       当前的流水记录标签编号
     * @param oldAmount   流水记录原本的金额
     * @param amount      流水记录当前的金额
     * @param type        流水记录种类
     * @param oldDatetime 原本的流水记录日期
     * @param datetime    当前的流水记录日期
     * @param db          能够写入数据的数据库实例
     * @throws SQLiteException 数据写入失败引发的异常
     */
    public static void onAccountUpdated(
            long oldTagNo,
            long tagNo,
            double oldAmount,
            double amount,
            RunningAccountType type,
            String oldDatetime,
            String datetime,
            SQLiteDatabase db,
            Context context
    ) throws SQLiteException {
        if (oldTagNo == tagNo && oldDatetime.substring(0, 10).equals(datetime.substring(0, 10))) {
            //如果新旧标签编号和日期相同，则直接用新余额减旧余额
            if (type.isExpenseType()) {
                decreaseLeftAmount(tagNo, amount - oldAmount, datetime, db, context);
            } else if (type.isIncomeType()) {
                increaseLeftAmount(tagNo, amount - oldAmount, datetime, db, context);
            }
        } else {
            //否则，先撤销旧流水记录的影响，然后再应用新流水记录的影响
            if (type.isExpenseType()) {
                increaseLeftAmount(oldTagNo, oldAmount, oldDatetime, db, context);
                decreaseLeftAmount(tagNo, amount, datetime, db, context);
            } else if (type.isIncomeType()) {
                decreaseLeftAmount(oldTagNo, oldAmount, oldDatetime, db, context);
                increaseLeftAmount(tagNo, amount, datetime, db, context);
            }
        }
    }

    /**
     * 删除一个标签记录
     *
     * @param tagNo 待被删除的标签的编号
     * @param db    可写的数据库实例
     * @throws SQLiteException 数据写入失败引发的异常
     */
    public static void onTagDeleted(long tagNo, @NonNull SQLiteDatabase db) throws SQLiteException {
        String where = Columns.TAG_NO + "=?";
        String[] whereArgs = {String.valueOf(tagNo)};
        db.delete(Tables.BUDGET_TAG.toString(), where, whereArgs);
    }

    /**
     * 重置预算
     *
     * @param bno     需要重置的预算编号
     * @param context 上下文
     */
    public static void resetBudget(long bno, Context context) {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //获取当前日期
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String nowStr = now.format(formatter);

        //生成 SQL
        String sql = "UPDATE " + Tables.BUDGET +
                " SET " + Columns.LEFT_AMOUNT + "=" + Columns.INIT_AMOUNT + "," +
                Columns.START_DATE + "=?" +
                " WHERE " + Columns.BNO + "=?";
        SQLiteStatement statement = db.compileStatement(sql);

        //绑定数据
        statement.bindString(1, nowStr);
        statement.bindDouble(2, bno);

        //执行 SQL 语句
        statement.executeUpdateDelete();

        db.close();
    }

    /**
     * 自动重置需要被重置的预算
     *
     * @param context 上下文
     * @throws SQLiteException 数据写入失败引发的异常
     */
    public static void resetAutomaticallyIfNeed(Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openWriteLink();

        //获取需要重置的预算编号
        List<Long> bnoList = getBnoNeedReset(db);

        //生成 SQL 语句
        String sql = "UPDATE " + Tables.BUDGET +
                " SET " + Columns.LEFT_AMOUNT + "=" + Columns.INIT_AMOUNT + "," +
                Columns.START_DATE + "=?" +
                " WHERE " + Columns.BNO + " IN (" + TextUtils.join(",", Collections.nCopies(bnoList.size(), "?")) +
                ")";
        SQLiteStatement statement = db.compileStatement(sql);

        //绑定 SQL 中的数据
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayStr = today.format(formatter);
        statement.bindString(1, todayStr);
        for (int i = 0; i < bnoList.size(); i++) {
            statement.bindLong(i + 2, bnoList.get(i));
        }

        //执行 SQL 语句
        statement.executeUpdateDelete();

        db.close();
    }

    /**
     * 获取需要重置的预算编号
     *
     * @param db 可读数据库实例
     * @return 需要重置的预算编号的列表
     * @throws SQLiteException 读取失败引发的异常
     */
    @NonNull
    private static List<Long> getBnoNeedReset(@NonNull SQLiteDatabase db) throws SQLiteException {
        //获取当前日期
        LocalDate today = LocalDate.now();

        //查询数据
        String[] columns = {
                Columns.START_DATE.toString(),
                Columns.RESET_FREQUENCY.toString(),
                Columns.BNO.toString()
        };
        Cursor budgetCursor = db.query(
                Tables.BUDGET.toString(),
                columns,
                null,
                null,
                null,
                null,
                null
        );

        //将需要重置的预算编号添加至列表
        List<Long> bnoList = new ArrayList<>();
        while (budgetCursor.moveToNext()) {
            long bno = budgetCursor.getLong(budgetCursor.getColumnIndexOrThrow(Columns.BNO.toString()));
            ResetFrequency frequency = ResetFrequency.valueOf(budgetCursor.getString(budgetCursor.getColumnIndexOrThrow(Columns.RESET_FREQUENCY.toString())));
            LocalDate startDate = LocalDate.parse(budgetCursor.getString(budgetCursor.getColumnIndexOrThrow(Columns.START_DATE.toString())));

            switch (frequency) {
                case EVERY_DAY:
                    bnoList.add(bno);
                    break;
                case EVERY_WEEK:
                    if (ChronoUnit.DAYS.between(today, startDate) >= 7) {
                        bnoList.add(bno);
                    }
                    break;
                case EVERY_MONTH:
                    if (today.getDayOfMonth() == 1) {
                        bnoList.add(bno);
                    }
            }
        }

        budgetCursor.close();
        return bnoList;
    }

    /**
     * 获取数据库中保存的预算数量
     *
     * @param context 上下文
     * @return 预算数量
     * @throws SQLiteException 数据读取失败引发的异常
     */
    public static int getDbCount(Context context) throws SQLiteException {
        BookkeepingDbHelper dbHelper = new BookkeepingDbHelper(context);
        SQLiteDatabase db = dbHelper.openReadLink();

        String sql = "SELECT COUNT(*) FROM " + Tables.BUDGET;
        int rowCount = 0;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            rowCount = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return rowCount;
    }
}
