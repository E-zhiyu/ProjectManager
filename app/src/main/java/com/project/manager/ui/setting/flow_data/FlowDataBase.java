package com.project.manager.ui.setting.flow_data;

//流水数据POJO类
public class FlowDataBase {
    private final String type;        //种类
    private final String remark;      //备注
    private final String date_time;   //日期和时间
    private final long tag_no;        //标签编号
    private final double amount;      //金额
    private final long fno;           //流水编号

    public String getType() {
        return type;
    }

    public String getRemark() {
        return remark;
    }

    public String getDate_time() {
        return date_time;
    }

    public long getTag_no() {
        return tag_no;
    }

    public double getAmount() {
        return amount;
    }

    public long getFno() {
        return fno;
    }

    FlowDataBase(String type, String remark, String date_time, long tag_no, double amount, long fno) {
        this.type = type;
        this.remark = remark;
        this.date_time = date_time;
        this.tag_no = tag_no;
        this.amount = amount;
        this.fno = fno;
    }
}

class ExpenseFlowData extends FlowDataBase {
    ExpenseFlowData(String type, String remark, String date_time, long tag_no, double amount, long fno) {
        super(type, remark, date_time, tag_no, amount, fno);
    }
}

class IncomeFlowData extends FlowDataBase {
    IncomeFlowData(String type, String remark, String date_time, long tag_no, double amount, long fno) {
        super(type, remark, date_time, tag_no, amount, fno);
    }
}

class TransferFlowData extends FlowDataBase {
    private String export_account, import_account;

    TransferFlowData(String type, String remark, String date_time, long tag_no, double amount, long fno,
                     String export_account, String import_account) {
        super(type, remark, date_time, tag_no, amount, fno);
        this.export_account = export_account;
        this.import_account = import_account;
    }
}
