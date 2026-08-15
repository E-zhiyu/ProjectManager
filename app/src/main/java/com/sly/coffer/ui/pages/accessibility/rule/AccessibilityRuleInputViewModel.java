package com.sly.coffer.ui.pages.accessibility.rule;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sly.coffer.auxiliary.classes.PickResult;
import com.sly.coffer.auxiliary.enums.AccountType;

public class AccessibilityRuleInputViewModel extends ViewModel {
    private final MutableLiveData<PickResult> pickResult = new MutableLiveData<>(null); //视图拾取结果
    private AccountType type = AccountType.EXPENSE;         //流水种类
    private int capturePos = 1;                             //金额文本捕获组位置

    public LiveData<PickResult> getPickResult() {
        return pickResult;
    }

    public void setPickResult(PickResult pickResult) {
        this.pickResult.postValue(pickResult);
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public int getCapturePos() {
        return capturePos;
    }

    public void setCapturePos(int capturePos) {
        this.capturePos = capturePos;
    }
}
