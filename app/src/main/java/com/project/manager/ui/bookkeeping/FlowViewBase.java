package com.project.manager.ui.bookkeeping;

import com.project.manager.ui.bookkeeping.flow_type.FlowTypeEnum;

public abstract class FlowViewBase {
    String name;        //名称
    FlowTypeEnum type;  //种类
    String remark;      //备注
    String date;        //日期
    double amount;         //金额
}

/**
 * 支出流水类
 */
class ExpenseFlowView extends FlowViewBase {
    public ExpenseFlowView(String remark, String date, double amount) {
        this.type = FlowTypeEnum.EXPENSE;
        this.name = "支出";
        this.remark = remark;
        this.date = date;
        this.amount = amount;
    }
}

/**
 * 收入流水类
 */
class IncomeFlowView extends FlowViewBase {
    public IncomeFlowView(String remark, String date, double amount) {
        this.type = FlowTypeEnum.INCOME;
        this.name = "收入";
        this.remark = remark;
        this.date = date;
        this.amount = amount;
    }
}

/**
 * 转账流水类
 */
class TransferFlowView extends FlowViewBase {
    String exportAccount;   //转出账户
    String importAccount;   //转入账户

    public TransferFlowView(String remark, String date, double amount, String exportAccount, String importAccount) {
        this.type = FlowTypeEnum.TRANSFER;
        this.name = "转账";
        this.remark = remark;
        this.date = date;
        this.amount = amount;
        this.exportAccount = exportAccount;
        this.importAccount = importAccount;
    }
}
