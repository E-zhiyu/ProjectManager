package com.manager.assistant.data.io.pojo;

public class PojoRuleAccount {
    private long ruleNo;
    private String exportAccount;
    private String importAccount;

    public PojoRuleAccount() {

    }

    public long getRuleNo() {
        return ruleNo;
    }

    public void setRuleNo(long ruleNo) {
        this.ruleNo = ruleNo;
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
