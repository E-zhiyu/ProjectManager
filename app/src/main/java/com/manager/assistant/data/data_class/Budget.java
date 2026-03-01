package com.manager.assistant.data.data_class;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import com.manager.assistant.data.data_save.database.BookkeepingColumns;
import com.manager.assistant.data.data_save.database.BookkeepingDbHelper;
import com.manager.assistant.data.data_save.database.BookkeepingTables;
import com.manager.assistant.ui.pages.bookkeeping.budget.ResetFrequency;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

import java.util.ArrayList;
import java.util.List;

public class Budget {
    private long bno;                       //预算编号
    private final String name;              //预算名称
    private final double initAmount;        //初始金额
    private final double leftAmount;        //剩余金额
    private final String startDate;         //起算日期
    private final ResetFrequency resetFrequency;    //重置频率
    private final List<Long> tagNoList;     //监听的标签的编号

    /**
     * 完整的构造方法
     *
     * @param bno            预算编号
     * @param name           预算名称
     * @param initAmount     初始金额
     * @param leftAmount     剩余金额
     * @param startDate      起算日期
     * @param resetFrequency 重置频率
     * @param tagNoList      标签编号列表
     */
    public Budget(
            long bno,
            String name,
            double initAmount,
            double leftAmount,
            String startDate,
            ResetFrequency resetFrequency,
            List<Long> tagNoList
    ) {
        this.bno = bno;
        this.name = name;
        this.initAmount = initAmount;
        this.leftAmount = leftAmount;
        this.startDate = startDate;
        this.resetFrequency = resetFrequency;
        this.tagNoList = tagNoList;
    }

    /**
     * 不指定编号的构造方法
     *
     * @param name           预算名称
     * @param initAmount     初始金额
     * @param leftAmount     剩余金额
     * @param startDate      起算日期
     * @param resetFrequency 重置频率
     * @param tagNoList      标签编号列表
     */
    public Budget(
            String name,
            double initAmount,
            double leftAmount,
            String startDate,
            ResetFrequency resetFrequency,
            List<Long> tagNoList
    ) {
        this(0, name, initAmount, leftAmount, startDate, resetFrequency, tagNoList);
    }

    /**
     * 不指定剩余金额的构造方法(剩余金额与初始金额相等
     *
     * @param name           预算名称
     * @param initAmount     初始金额
     * @param startDate      起算日期
     * @param resetFrequency 重置频率
     * @param tagNoList      标签编号列表
     */
    public Budget(
            String name,
            double initAmount,
            String startDate,
            ResetFrequency resetFrequency,
            List<Long> tagNoList
    ) {
        this(name, initAmount, initAmount, startDate, resetFrequency, tagNoList);
    }

    public long getBno() {
        return bno;
    }

    public void setBno(long bno) {
        this.bno = bno;
    }

    public String getName() {
        return name;
    }

    public double getInitAmount() {
        return initAmount;
    }

    public double getLeftAmount() {
        return leftAmount;
    }

    public String getStartDate() {
        return startDate;
    }

    public ResetFrequency getResetFrequency() {
        return resetFrequency;
    }

    public List<Long> getTagNoList() {
        return tagNoList;
    }

    /**
     * 通过标签编号获取预算编号
     *
     * @param db       数据库实例
     * @param tagNo    标签编号
     * @return 包含传入的标签编号的预算编号列表
     * @throws SQLiteException 数据读取失败引发的异常
     */
    @NonNull
    private static List<Long> getBnoByTagNo(@NonNull SQLiteDatabase db, long tagNo) throws SQLiteException {
        String selection = BookkeepingColumns.TAG_NO + "=?";
        String[] selectionArgs = {String.valueOf(tagNo)};
        String[] columns = {
                BookkeepingColumns.BNO.toString()
        };
        Cursor budgetCursor = db.query(
                BookkeepingTables.BUDGET_TAG.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        List<Long> bnoList = new ArrayList<>();
        while (budgetCursor.moveToNext()) {
            long bno = budgetCursor.getLong(budgetCursor.getColumnIndexOrThrow(BookkeepingColumns.BNO.toString()));
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
                BookkeepingColumns.TAG_NO.toString()
        };
        String selection = BookkeepingColumns.BNO + "=?";
        String[] selectionArgs = {String.valueOf(bno)};
        Cursor cursor = db.query(
                BookkeepingTables.BUDGET_TAG.toString(),
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        List<Long> tagNoList = new ArrayList<>();
        while (cursor.moveToNext()) {
            long tagNo = cursor.getLong(cursor.getColumnIndexOrThrow(BookkeepingColumns.TAG_NO.toString()));
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
            values.put(BookkeepingColumns.TAG_NO.toString(), tagNo);
            values.put(BookkeepingColumns.BNO.toString(), bno);
            db.insert(BookkeepingTables.BUDGET_TAG.toString(), null, values);

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
        String where = BookkeepingColumns.BNO + "=?";
        String[] whereArgs = {String.valueOf(bno)};

        db.delete(BookkeepingTables.BUDGET_TAG.toString(), where, whereArgs);
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
                BookkeepingTables.BUDGET.toString(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        List<Budget> budgetList = new ArrayList<>();
        while (budgetCursor.moveToNext()) {
            long bno = budgetCursor.getLong(budgetCursor.getColumnIndexOrThrow(BookkeepingColumns.BNO.toString()));
            String name = budgetCursor.getString(budgetCursor.getColumnIndexOrThrow(BookkeepingColumns.BUDGET_NAME.toString()));
            double initAmount = budgetCursor.getDouble(budgetCursor.getColumnIndexOrThrow(BookkeepingColumns.INIT_AMOUNT.toString()));
            double leftAmount = budgetCursor.getDouble(budgetCursor.getColumnIndexOrThrow(BookkeepingColumns.LEFT_AMOUNT.toString()));
            String startDate = budgetCursor.getString(budgetCursor.getColumnIndexOrThrow(BookkeepingColumns.START_DATE.toString()));
            String resetFrequencyStr = budgetCursor.getString(budgetCursor.getColumnIndexOrThrow(BookkeepingColumns.RESET_FREQUENCY.toString()));
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
        budgetValues.put(BookkeepingColumns.BUDGET_NAME.toString(), name);
        budgetValues.put(BookkeepingColumns.INIT_AMOUNT.toString(), initAmount);
        budgetValues.put(BookkeepingColumns.LEFT_AMOUNT.toString(), leftAmount);
        budgetValues.put(BookkeepingColumns.START_DATE.toString(), startDate);
        budgetValues.put(BookkeepingColumns.RESET_FREQUENCY.toString(), resetFrequency.toString());
        long bno = db.insert(BookkeepingTables.BUDGET.toString(), null, budgetValues);
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
        String where = BookkeepingColumns.BNO + "=?";
        String[] whereArgs = {String.valueOf(bno)};
        ContentValues budgetValues = new ContentValues();
        budgetValues.put(BookkeepingColumns.BUDGET_NAME.toString(), name);
        budgetValues.put(BookkeepingColumns.INIT_AMOUNT.toString(), initAmount);
        budgetValues.put(BookkeepingColumns.LEFT_AMOUNT.toString(), leftAmount);
        budgetValues.put(BookkeepingColumns.START_DATE.toString(), startDate);
        budgetValues.put(BookkeepingColumns.RESET_FREQUENCY.toString(), resetFrequency.toString());
        db.update(BookkeepingTables.BUDGET.toString(), budgetValues, where, whereArgs);
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

        String where = BookkeepingColumns.BNO + "=?";
        String[] whereArgs = {String.valueOf(bno)};
        deleteTagNoWithBno(db, bno);
        db.delete(BookkeepingTables.BUDGET.toString(), where, whereArgs);

        db.close();
    }

    /**
     * 增加剩余金额
     *
     * @param tagNo    流水记录的标签编号
     * @param amount   剩余金额增加的值(可以为负数)
     * @param datetime 流水记录的日期
     * @param db       需要写入数据的数据库实例
     * @throws SQLiteException 数据写入失败引发的异常
     */
    private static void increaseLeftAmount(
            long tagNo,
            double amount,
            @NonNull String datetime,
            SQLiteDatabase db
    ) throws SQLiteException {
        //将datetime转换为date
        if (datetime.length() > 10) {
            datetime = datetime.substring(0, 10);
        }

        //TODO:使用SQL语句嵌套查询优化该逻辑
        //获取需要更改的预算编号列表
        List<Long> bnoList = getBnoByTagNo(db, tagNo);

        //写入数据
        ContentValues values = new ContentValues();
        for (long bno : bnoList) {
            String selection = BookkeepingColumns.BNO + "=? AND " + BookkeepingColumns.START_DATE + "<?";
            String[] selectionArgs = {
                    String.valueOf(bno),
                    datetime
            };
            String[] columns = {
                    BookkeepingColumns.LEFT_AMOUNT.toString()
            };
            Cursor cursor = db.query(
                    BookkeepingTables.BUDGET.toString(),
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null,
                    "1"
            );

            if (cursor.moveToNext()) {
                double oldAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(BookkeepingColumns.LEFT_AMOUNT.toString()));
                values.put(BookkeepingColumns.LEFT_AMOUNT.toString(), amount + oldAmount);
                db.update(BookkeepingTables.BUDGET.toString(), values, selection, selectionArgs);
            }

            cursor.close();
        }
    }

    /**
     * 减少剩余金额
     *
     * @param tagNo    流水记录的标签编号
     * @param amount   剩余金额减少的值(可以为负数)
     * @param datetime 流水记录的日期
     * @param db       需要写入数据的数据库实例
     * @throws SQLiteException 数据写入失败引发的异常
     */
    private static void decreaseLeftAmount(
            long tagNo,
            double amount,
            String datetime,
            SQLiteDatabase db
    ) throws SQLiteException {
        increaseLeftAmount(tagNo, -amount, datetime, db);
    }

    /**
     * 处理新旧日期差别的方法
     *
     * @param tagNo       当前的流水记录标签编号
     * @param amount      流水记录金额变化量(新减旧)
     * @param type        流水记录种类
     * @param oldDatetime 原本的流水记录日期
     * @param datetime    当前的流水记录日期
     * @param db          能够写入数据的数据库实例
     * @throws SQLiteException 数据写入失败引发的异常
     */
    private static void processDatetimeDifference(
            long tagNo,
            double amount,
            RunningAccountType type,
            @NonNull String oldDatetime,
            @NonNull String datetime,
            SQLiteDatabase db
    ) throws SQLiteException {
        if (oldDatetime.substring(0, 10).equals(datetime.substring(0, 10))) {
            if (type.isExpenseType()) {
                decreaseLeftAmount(tagNo, amount, datetime, db);
            } else if (type.isIncomeType()) {
                increaseLeftAmount(tagNo, amount, datetime, db);
            }
        } else {
            if (type.isExpenseType()) {
                increaseLeftAmount(tagNo, amount, oldDatetime, db);
                decreaseLeftAmount(tagNo, amount, datetime, db);
            } else if (type.isIncomeType()) {
                decreaseLeftAmount(tagNo, amount, oldDatetime, db);
                increaseLeftAmount(tagNo, amount, datetime, db);
            }
        }
    }

    /**
     * 处理流水记录更新的方法
     *
     * @param oldTagNo    原本的流水记录标签编号
     * @param tagNo       当前的流水记录标签编号
     * @param amount      流水记录金额变化量(新减旧)
     * @param type        流水记录种类
     * @param oldDatetime 原本的流水记录日期
     * @param datetime    当前的流水记录日期
     * @param db          能够写入数据的数据库实例
     * @throws SQLiteException 数据写入失败引发的异常
     */
    public static void onAccountUpdated(
            long oldTagNo,
            long tagNo,
            double amount,
            RunningAccountType type,
            String oldDatetime,
            String datetime,
            SQLiteDatabase db
    ) throws SQLiteException {
        if (oldTagNo == tagNo) {
            processDatetimeDifference(tagNo, amount, type, oldDatetime, datetime, db);
        } else {
            processDatetimeDifference(oldTagNo, amount, type, oldDatetime, datetime, db);
            processDatetimeDifference(tagNo, amount, type, oldDatetime, datetime, db);
        }
    }
}
