package com.manager.assistant.data.io.pojos;

import androidx.annotation.Keep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//转账流水数据POJO类
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中多余字段
@Keep
public class PojoTransferRunningAccount {
    private String export_account, import_account;
    private long rno;

    public long getRno() {
        return rno;
    }

    public void setRno(long rno) {
        this.rno = rno;
    }

    public String getExport_account() {
        return export_account;
    }

    public void setExport_account(String export_account) {
        this.export_account = export_account;
    }

    public String getImport_account() {
        return import_account;
    }

    public void setImport_account(String import_account) {
        this.import_account = import_account;
    }

    public PojoTransferRunningAccount(long rno, String export_account, String import_account) {
        this.rno = rno;
        this.export_account = export_account;
        this.import_account = import_account;
    }

    public PojoTransferRunningAccount() {

    }
}
