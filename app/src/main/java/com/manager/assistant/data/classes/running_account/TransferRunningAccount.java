package com.manager.assistant.data.classes.running_account;

import androidx.annotation.NonNull;

import com.manager.assistant.auxiliary.enums.AccountType;

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
     * @param dateTime      日期
     * @param amount        金额
     * @param exportAccount 转出账户
     * @param importAccount 转入账户
     */
    public TransferRunningAccount(@NonNull String remark, String dateTime, double amount, String exportAccount, String importAccount) {
        super();
        this.rno = -1;
        this.type = AccountType.TRANSFER;
        this.title = "转账";
        this.remark = remark;
        this.datetime = dateTime;
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
        this.type = AccountType.TRANSFER;
        this.title = "转账";
        this.remark = remark;
        this.datetime = date_time;
        this.amount = amount;
        this.exportAccount = exportAccount;
        this.importAccount = importAccount;
    }

    @Override
    protected String initDefaultRemark() {
        return "一条转账记录";
    }
}
