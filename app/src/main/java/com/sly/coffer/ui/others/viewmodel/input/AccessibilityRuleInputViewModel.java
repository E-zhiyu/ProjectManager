package com.sly.coffer.ui.others.viewmodel.input;

import androidx.lifecycle.ViewModel;

import com.sly.coffer.auxiliary.classes.PickResult;
import com.sly.coffer.auxiliary.enums.AccountType;

public class AccessibilityRuleInputViewModel extends ViewModel {
    private PickResult pickResult = null;           //视图拾取结果
    private AccountType type = AccountType.EXPENSE; //流水种类

    public PickResult getPickResult() {
        return pickResult;
    }

    public void setPickResult(PickResult pickResult) {
        this.pickResult = pickResult;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }
}
