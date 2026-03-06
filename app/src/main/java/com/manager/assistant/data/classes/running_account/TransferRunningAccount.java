package com.manager.assistant.data.classes.running_account;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import com.manager.assistant.data.save.database.Columns;
import com.manager.assistant.data.save.database.BookkeepingDbHelper;
import com.manager.assistant.data.save.database.Tables;
import com.manager.assistant.ui.pages.bookkeeping.running_account.fragments.RunningAccountType;

import java.util.HashSet;

/**
 * 转账流水类
 */
public class TransferRunningAccount extends RunningAccountBase {
    private final String exportAccount; //转出账户
    private final String importAccount; //转入账户

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
    public TransferRunningAccount(@NonNull String remark, String date_time, double amount, String exportAccount, String importAccount) {
        super();
        this.rno = -1;
        this.type = RunningAccountType.TRANSFER;
        this.title = "转账";
        this.remark = remark;
        this.datetime = date_time;
        this.amount = amount;
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
    public TransferRunningAccount(long rno, @NonNull String remark, String date_time, double amount, String exportAccount, String importAccount) {
        super();
        this.rno = rno;
        this.type = RunningAccountType.TRANSFER;
        this.title = "转账";
        this.remark = remark;
        this.datetime = date_time;
        this.amount = amount;
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

    @Override
    protected String initDefaultRemark() {
        return "一条转账记录";
    }
}
