package com.manager.assistant.ui.sync.budget;

import com.manager.assistant.data.classes.Budget;
import com.manager.assistant.ui.sync.UnPeekLiveData;

public class BudgetRepository {
    private static BudgetRepository instance;   //自身类的实例，确保通过getInstance()获取到的是同一个实例
    private BudgetUpdateReason updateReason;    //更新原因
    private final UnPeekLiveData<Budget> changedBudget = new UnPeekLiveData<>();    //动态数据

    /**
     * 获取唯一实例
     *
     * @return 自身的实例
     */
    public static BudgetRepository getInstance() {
        if (instance == null) {
            instance = new BudgetRepository();
        }
        return instance;
    }

    public UnPeekLiveData<Budget> getChangedBudget() {
        return changedBudget;
    }

    public BudgetUpdateReason getUpdateReason() {
        return updateReason;
    }

    /**
     * 预算更新回调
     *
     * @param budget 更新后的预算实例
     * @param reason 更新原因
     */
    public void onUpdated(Budget budget, BudgetUpdateReason reason) {
        this.updateReason = reason;
        changedBudget.postValue(budget);
    }

    /**
     * 不传递预算实例的更新回调（如清除、导入）
     *
     * @param reason 更新原因
     */
    public void onUpdated(BudgetUpdateReason reason) {
        onUpdated(null, reason);
    }
}
