package com.sly.coffer.ui.pages.accessibility.rule;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sly.coffer.auxiliary.enums.AccountType;
import com.sly.coffer.data.save.db.entities.PickedViewEntity;

public class AccessibilityRuleInputViewModel extends ViewModel {
    private final MutableLiveData<PickedViewEntity> pickResult = new MutableLiveData<>(null); //视图拾取结果
    private AccountType type = AccountType.EXPENSE;         //流水种类
    private int capturePos = 1;                             //金额文本捕获组位置

    public LiveData<PickedViewEntity> getPickedView() {
        return pickResult;
    }

    public void setPickResult(PickedViewEntity pickedView) {
        this.pickResult.postValue(pickedView);
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
