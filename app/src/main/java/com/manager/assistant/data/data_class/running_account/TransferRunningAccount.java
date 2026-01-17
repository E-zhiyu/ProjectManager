package com.manager.assistant.data.data_class.running_account;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import com.manager.assistant.data.data_save.database.BookKeepingColumns;
import com.manager.assistant.data.data_save.database.BookKeepingTables;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

/**
 * 转账流水类
 */
public class TransferRunningAccount extends RunningAccountBase {
    protected final String default_remark = "一条转账记录";
    String exportAccount;   //转出账户
    String importAccount;   //转入账户

    public String getExportAccount() {
        return exportAccount;
    }

    public String getImportAccount() {
        return importAccount;
    }

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
        this.datetime = date_time;
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
        this.datetime = date_time;
        this.amount = amount;
        this.isDefaultRemark = isDefaultRemark;
        this.exportAccount = exportAccount;
        this.importAccount = importAccount;
    }

    /**
     * 清空转账流水记录特有的数据
     *
     * @param rno 流水记录编号
     * @param db  需要修改的数据库
     * @throws SQLiteException 数据库修改失败引发的异常
     */
    public static void deleteTransferAccount(long rno, @NonNull SQLiteDatabase db) throws SQLiteException {
        String where = BookKeepingColumns.RNO + "=?";
        String[] whereArgs = {String.valueOf(rno)};
        db.delete(BookKeepingTables.TRANSFER.toString(), where, whereArgs);
    }
}
