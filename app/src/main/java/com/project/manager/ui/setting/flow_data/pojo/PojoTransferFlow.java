package com.project.manager.ui.setting.flow_data.pojo;

//转账流水数据POJO类
public class PojoTransferFlow {
    private String export_account, import_account;
    private long fno;

    public long getFno() {
        return fno;
    }

    public void setFno(long fno) {
        this.fno = fno;
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

    public PojoTransferFlow(long fno, String export_account, String import_account) {
        this.fno = fno;
        this.export_account = export_account;
        this.import_account = import_account;
    }

    public PojoTransferFlow() {

    }
}
