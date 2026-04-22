package com.manager.assistant.ui.sync.account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.manager.assistant.ui.pages.main.bookkeeping.fragments.RunningAccountType;

public class RunningAccountRepository {
    private static RunningAccountRepository instance;    //自身实例
    private AccountUpdateReason updateReason;           //更新原因
    private final MutableLiveData<SimpleRunningAccount> accountData = new MutableLiveData<>();

    public static class SimpleRunningAccount {
        public double amount;
        public String datetime;
        public RunningAccountType type;

        public SimpleRunningAccount(double amount, String datetime, RunningAccountType type) {
            this.amount = amount;
            this.datetime = datetime;
            this.type = type;
        }
    }

    public static RunningAccountRepository getInstance() {
        if (instance == null) {
            instance = new RunningAccountRepository();
        }
        return instance;
    }

    public AccountUpdateReason getUpdateReason() {
        return updateReason;
    }

    public LiveData<SimpleRunningAccount> getAccountData() {
        return accountData;
    }

    /**
     * 流水更新回调
     *
     * @param amount   金额
     * @param datetime 更新的流水记录的日期和时间
     * @param type     流水种类
     * @param reason   更新原因
     */
    public void onAccountUpdated(
            double amount,
            String datetime,
            RunningAccountType type,
            AccountUpdateReason reason) {
        updateReason = reason;
        SimpleRunningAccount runningAccount = new SimpleRunningAccount(amount, datetime, type);
        accountData.postValue(runningAccount);
    }
}
