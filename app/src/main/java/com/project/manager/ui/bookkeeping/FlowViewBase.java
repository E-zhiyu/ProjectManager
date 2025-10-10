package com.project.manager.ui.bookkeeping;

import com.project.manager.ui.bookkeeping.flow_type.FlowTypeEnum;

public abstract class FlowViewBase {
    String name;        //名称
    FlowTypeEnum type;  //种类
    String remark;      //备注
    String date;        //日期
    int amount;         //金额
}

/**
 * 支出流水类
 */
class ExpenseFlowView extends FlowViewBase {
    public ExpenseFlowView(FlowTypeEnum type, String name, String remark, String date, int amount) {
        this.type = type;
        this.name = name;
        this.remark = remark;
        this.date = date;
        this.amount = amount;
    }
}

/**
 * 收入流水类
 */
class IncomeFlowView extends FlowViewBase {
    public IncomeFlowView(FlowTypeEnum type, String name, String remark, String date, int amount) {
        this.type = type;
        this.name = name;
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

    public TransferFlowView(FlowTypeEnum type, String name, String remark, String date, int amount, String exportAccount, String importAccount) {
        this.type = type;
        this.name = name;
        this.remark = remark;
        this.date = date;
        this.amount = amount;
        this.exportAccount = exportAccount;
        this.importAccount = importAccount;
    }
}
