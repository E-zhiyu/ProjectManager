package com.manager.assistant.data.save.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "transferRuleAccounts",
        indices = {
                @Index(value = "ruleId")
        }
)
public class TransferRuleAccountEntity {
    @PrimaryKey(autoGenerate = true)
    private long transferId;        //主键
    private long ruleId;            //规则编号
    private String exportAccount;   //转出账户
    private String importAccount;   //转入账户
}
