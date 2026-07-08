package com.manager.assistant.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "accountTransfers",
        foreignKeys = @ForeignKey(
                entity = AccountEntity.class,
                parentColumns = "accountId",
                childColumns = "accountId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = "accountId")
        }
)
public class AccountTransferEntity {
    @PrimaryKey(autoGenerate = true)
    private long transferId;        //主键
    private long accountId;         //流水账 ID
    private String exportAccount;   //转出账户
    private String importAccount;   //转入账户

    public AccountTransferEntity(String exportAccount, String importAccount) {
        this.exportAccount = exportAccount;
        this.importAccount = importAccount;
    }

    public long getTransferId() {
        return transferId;
    }

    public void setTransferId(long transferId) {
        this.transferId = transferId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getExportAccount() {
        return exportAccount;
    }

    public void setExportAccount(String exportAccount) {
        this.exportAccount = exportAccount;
    }

    public String getImportAccount() {
        return importAccount;
    }

    public void setImportAccount(String importAccount) {
        this.importAccount = importAccount;
    }
}
