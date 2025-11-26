package com.project.manager.ui.bookkeeping;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import com.project.manager.database.BookKeepingColumns;
import com.project.manager.database.BookKeepingDatabaseHelper;
import com.project.manager.database.BookKeepingTables;
import com.project.manager.ui.bookkeeping.running_account_edit.fragments.RunningAccountType;

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
        BookKeepingDatabaseHelper db_helper = new BookKeepingDatabaseHelper(context);
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
}

/**
 * 支出流水类
 */
class ExpenseRunningAccount extends RunningAccountBase {
    protected final String default_remark = "一条支出记录";

    /**
     * 不给定编号的构造方法
     *
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     */
    public ExpenseRunningAccount(@NonNull String remark, String date_time, double amount, boolean isDefaultRemark) {
        this.type = RunningAccountType.EXPENSE;
        this.name = "支出";
        this.remark = remark.isEmpty() ? default_remark : remark;
        this.isDefaultRemark = remark.isEmpty();
        this.date_time = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
        this.rno = -1;
    }

    /**
     * 给定编号的构造方法（用于数据库查询时）
     *
     * @param rno       编号
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     */
    public ExpenseRunningAccount(long rno, @NonNull String remark, String date_time, double amount, boolean isDefaultRemark) {
        this.rno = rno;
        this.type = RunningAccountType.EXPENSE;
        this.name = "支出";
        this.remark = remark.isEmpty() ? default_remark : remark;
        this.date_time = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
    }
}

/**
 * 收入流水类
 */
class IncomeRunningAccount extends RunningAccountBase {
    protected final String default_remark = "一条收入记录";

    /**
     * 不给定编号的构造方法
     *
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     */
    public IncomeRunningAccount(@NonNull String remark, String date_time, double amount, boolean isDefaultRemark) {
        this.rno = -1;
        this.type = RunningAccountType.INCOME;
        this.name = "收入";
        this.remark = remark.isEmpty() ? default_remark : remark;
        this.date_time = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
    }

    /**
     * 给定编号的构造方法
     *
     * @param rno       编号
     * @param remark    备注
     * @param date_time 日期和时间
     * @param amount    金额
     */
    public IncomeRunningAccount(long rno, @NonNull String remark, String date_time, double amount, boolean isDefaultRemark) {
        this.rno = rno;
        this.type = RunningAccountType.INCOME;
        this.name = "收入";
        this.remark = remark.isEmpty() ? default_remark : remark;
        this.date_time = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
    }
}

/**
 * 转账流水类
 */
class TransferRunningAccount extends RunningAccountBase {
    protected final String default_remark = "一条转账记录";
    String exportAccount;   //转出账户
    String importAccount;   //转入账户

    /**
     * 不给定编号的构造方法
     *
     * @param remark        备注
     * @param date_time     日期
     * @param amount        金额
     * @param exportAccount 转出账户
     * @param importAccount 转入账户
     */
    public TransferRunningAccount(@NonNull String remark, String date_time, double amount, boolean isDefaultRemark, String exportAccount, String importAccount) {
        this.rno = -1;
        this.type = RunningAccountType.TRANSFER;
        this.name = "转账";
        this.remark = remark.isEmpty() ? default_remark : remark;
        this.date_time = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
        this.exportAccount = exportAccount;
        this.importAccount = importAccount;
    }

    /**
     * 给定编号的构造方法
     *
     * @param rno           编号
     * @param remark        备注
     * @param date_time     日期和时间
     * @param amount        金额
     * @param exportAccount 转出账户
     * @param importAccount 转入账户
     */
    public TransferRunningAccount(long rno, @NonNull String remark, String date_time, double amount, boolean isDefaultRemark, String exportAccount, String importAccount) {
        this.rno = rno;
        this.type = RunningAccountType.TRANSFER;
        this.name = "转账";
        this.remark = remark.isEmpty() ? default_remark : remark;
        this.date_time = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
        this.exportAccount = exportAccount;
        this.importAccount = importAccount;
    }
}
