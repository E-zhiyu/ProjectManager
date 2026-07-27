package com.sly.coffer.data.save.db.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "notificationRuleTransfers",
        indices = {
                @Index(value = "ruleId")
        }
)
public class NotificationRuleTransferEntity {
    @PrimaryKey(autoGenerate = true)
    private long transferId;        //主键
    private long ruleId;            //规则编号
    private String exportAccount;   //转出账户
    private String importAccount;   //转入账户

    public NotificationRuleTransferEntity(String importAccount, String exportAccount) {
        this.importAccount = importAccount;
        this.exportAccount = exportAccount;
    }

    public long getTransferId() {
        return transferId;
    }

    public void setTransferId(long transferId) {
        this.transferId = transferId;
    }

    public long getRuleId() {
        return ruleId;
    }

    public void setRuleId(long ruleId) {
        this.ruleId = ruleId;
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
